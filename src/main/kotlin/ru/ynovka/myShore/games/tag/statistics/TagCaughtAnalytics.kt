package ru.ynovka.myShore.games.tag.statistics

import com.github.darksoulq.abyssallib.common.database.relational.sql.Database
import org.bukkit.entity.Player
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.maps.TagGameMaps
import kotlin.math.sqrt

// ═══════════════════════════════════════════════════════════════════════════
//  Data classes
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Immutable 3D position. Used both for raw DB rows and cluster centroids.
 */
data class Pos3D(val x: Double, val y: Double, val z: Double)

/**
 * One point on the heatmap after clustering.
 *
 * @param position  centroid of the cluster (average of all merged points)
 * @param count     how many raw records were merged into this point
 * @param hexColor  CSS-style hex string, e.g. "#0000FF" (blue=1) … "#FF0000" (red=100+)
 *
 * Helper: get Bukkit Color for DustOptions
 *   val color = org.bukkit.Color.fromRGB(point.r, point.g, point.b)
 *   val dust  = Particle.DustOptions(color, 1.0f)
 */
data class HeatmapPoint(
    val position: Pos3D,
    val count:    Int,
    val hexColor: String,
) {
    /** Red channel 0-255 */
    val r: Int get() = hexColor.substring(1, 3).toInt(16)
    /** Green channel 0-255 */
    val g: Int get() = hexColor.substring(3, 5).toInt(16)
    /** Blue channel 0-255 */
    val b: Int get() = hexColor.substring(5, 7).toInt(16)
}

// ═══════════════════════════════════════════════════════════════════════════
//  Repository
// ═══════════════════════════════════════════════════════════════════════════

/**
 * All DB access for the `tag_caughts` table.
 *
 * Typical lifecycle:
 * ```kotlin
 * val repo = TagCaughtsRepository(sqliteDb)
 * repo.init()                          // once, on plugin enable
 *
 * // on DamageByEntityEvent:
 * repo.save(victim, hunter, game)
 *
 * // for analytics:
 * val points = repo.getVictimHeatmap("Steve", TagGameMaps.JUNGLE, limit = 200)
 * points.forEach { pt ->
 *     val color = org.bukkit.Color.fromRGB(pt.r, pt.g, pt.b)
 *     world.spawnParticle(Particle.DUST, pt.position.x, pt.position.y, pt.position.z,
 *                         1, 0.0, 0.0, 0.0, 0.0,
 *                         Particle.DustOptions(color, 1.0f))
 * }
 * ```
 */
class TagCaughtsRepository(private val db: Database) {

    // ── Table: tag_caughts ───────────────────────────────────────────────────
    //
    //  id          INTEGER  PRIMARY KEY AUTOINCREMENT
    //  victim_name TEXT     NOT NULL   — игрок 1 (кого поймали)
    //  hunter_name TEXT     NOT NULL   — игрок 2 (кто поймал)
    //  victim_x/y/z REAL   NOT NULL
    //  hunter_x/y/z REAL   NOT NULL
    //  map_name    TEXT     NOT NULL   — TagGameMaps.name()
    //
    // ────────────────────────────────────────────────────────────────────────

    /** Creates the table. Call once during plugin enable / db initialisation. */
    fun init() {
        db.executor()
            .create("tag_caughts")
            .ifNotExists()
            .autoIncrement("id")                      // id INTEGER PRIMARY KEY AUTOINCREMENT
            .column("victim_name", "TEXT NOT NULL")
            .column("hunter_name", "TEXT NOT NULL")
            .column("victim_x",    "REAL NOT NULL")
            .column("victim_y",    "REAL NOT NULL")
            .column("victim_z",    "REAL NOT NULL")
            .column("hunter_x",    "REAL NOT NULL")
            .column("hunter_y",    "REAL NOT NULL")
            .column("hunter_z",    "REAL NOT NULL")
            .column("map_name",    "TEXT NOT NULL")
            .execute()
    }

    /**
     * Saves one "catch" event.
     *
     * Example usage inside your listener:
     * ```kotlin
     * val victim  = event.entity  as? Player ?: return
     * val hunter  = event.damager as? Player ?: return
     * val game    = hunter.getLobby()?.game as? TagGame ?: return
     * repo.save(victim, hunter, game)
     * ```
     *
     * The map is resolved from [game.map.id] to the matching [TagGameMaps] enum entry.
     * If no match is found (e.g. a custom map) RANDOM is stored as a fallback.
     */
    fun save(victim: Player, hunter: Player, game: TagGame) {
        val vl = victim.location
        val hl = hunter.location

        // Resolve the currently active map back to the enum label so queries stay clean.
        // RANDOM acts as "unknown / other" when no enum id matches.
        val mapLabel = resolveMapLabel(game.map.mapId)

        db.executor()
            .table("tag_caughts")
            .insert()
            .value("victim_name", victim.name)
            .value("hunter_name", hunter.name)
            .value("victim_x",    vl.x)
            .value("victim_y",    vl.y)
            .value("victim_z",    vl.z)
            .value("hunter_x",    hl.x)
            .value("hunter_y",    hl.y)
            .value("hunter_z",    hl.z)
            .value("map_name",    mapLabel)
            .execute()
    }

    /**
     * Builds a heatmap of positions where [playerName] was caught.
     *
     * @param playerName  the victim's exact in-game name
     * @param map         which map to analyse; [TagGameMaps.RANDOM] means ALL maps
     * @param limit       number of most-recent records to include, or **null** for all
     *
     * @return list of [HeatmapPoint]s — clusters of raw positions coloured by density:
     *         - 1 hit   → blue  (#0000FF)
     *         - 100+    → red   (#FF0000)
     *         - between → smooth gradient
     *
     * Points within a 0.1-block radius (3D Euclidean) are merged into one cluster.
     */
    fun getVictimHeatmap(
        playerName: String,
        map: TagGameMaps,
        limit:      Int? = null,
    ): List<HeatmapPoint> {

        val query = db.executor().table("tag_caughts")

        // Build the WHERE clause depending on whether we filter by map
        if (map == TagGameMaps.RANDOM) {
            // RANDOM = no map filter → analyse across all maps
            query.where("victim_name = ?", playerName)
        } else {
            query.where("victim_name = ? AND map_name = ?", playerName, map.name)
        }

        query.orderBy("id", false)
        if (limit != null) query.limit(limit)

        val rawPositions = query.select { rs ->
            Pos3D(
                rs.getDouble("victim_x"),
                rs.getDouble("victim_y") + 1.0,
                rs.getDouble("victim_z"),
            )
        }

        return cluster(rawPositions)
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Tries to match a live map ID (e.g. "tag_jungle") to a [TagGameMaps] entry.
     * RANDOM entries are skipped because their provider returns a different map each time.
     */
    private fun resolveMapLabel(mapId: String): String {
        return TagGameMaps.entries
            .firstOrNull { entry ->
                entry != TagGameMaps.RANDOM &&
                        runCatching { entry.mapProvider().mapId == mapId }.getOrDefault(false)
            }
            ?.name
            ?: TagGameMaps.RANDOM.name   // fallback
    }

    /**
     * Greedy single-linkage clustering: a new point joins the first existing cluster
     * that has ANY member within [CLUSTER_RADIUS] blocks of it.
     *
     * Complexity: O(n²) — perfectly fine for the expected dataset sizes (<10 000 rows).
     * Swap for a spatial index (k-d tree) if you ever exceed that.
     */
    private fun cluster(points: List<Pos3D>): List<HeatmapPoint> {
        val clusters = mutableListOf<MutableList<Pos3D>>()

        for (point in points) {
            val target = clusters.firstOrNull { group ->
                group.any { p -> dist(p, point) < CLUSTER_RADIUS }
            }
            if (target != null) target.add(point)
            else                clusters.add(mutableListOf(point))
        }

        return clusters.map { group ->
            val cx = group.sumOf { it.x } / group.size
            val cy = group.sumOf { it.y } / group.size
            val cz = group.sumOf { it.z } / group.size
            HeatmapPoint(Pos3D(cx, cy, cz), group.size, heatColor(group.size))
        }
    }

    private fun dist(a: Pos3D, b: Pos3D): Double {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dz = a.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Maps a cluster count to a hex color.
     *
     * count = 1   → #0000FF (pure blue)
     * count = 100 → #FF0000 (pure red)
     * Linear RGB interpolation between the two.
     *
     *  t = 0.0 … 1.0
     *  R = round(t * 255)
     *  G = 0
     *  B = round((1-t) * 255)
     */
    private fun heatColor(count: Int): String {
        val t = ((count - 1).coerceIn(0, 99) / 99.0)
        val r = (t * 255).toInt()
        val b = ((1.0 - t) * 255).toInt()
        return "#%02X%02X%02X".format(r, 0, b)
    }

    companion object {
        /** Points closer than this (blocks) are merged into one cluster. */
        const val CLUSTER_RADIUS = 0.1
    }
}
