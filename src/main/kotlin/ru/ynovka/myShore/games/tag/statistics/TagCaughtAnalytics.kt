package ru.ynovka.myShore.games.tag.statistics

import com.github.darksoulq.abyssallib.common.database.relational.sql.Database
import ru.ynovka.myShore.games.tag.maps.TagMaps
import ru.ynovka.myShore.games.tag.TagGame
import org.bukkit.entity.Player
import kotlin.math.sqrt

data class Pos3D(val x: Double, val y: Double, val z: Double)

data class HeatmapPoint(
    val position: Pos3D,
    val count:    Int,
    val hexColor: String,
) {
    val r: Int get() = hexColor.substring(1, 3).toInt(16)
    val g: Int get() = hexColor.substring(3, 5).toInt(16)
    val b: Int get() = hexColor.substring(5, 7).toInt(16)
}

class TagCaughtsRepository(private val db: Database) {

    fun init() {
        db.executor()
            .create("tag_caughts")
            .ifNotExists()
            .autoIncrement("id")
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

    fun save(victim: Player, hunter: Player, game: TagGame) {
        val vl = victim.location
        val hl = hunter.location

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

    fun getVictimHeatmap(
        playerName: String,
        map: TagMaps,
        limit:      Int? = null,
    ): List<HeatmapPoint> {

        val query = db.executor().table("tag_caughts")

        if (map == TagMaps.RANDOM) {
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

    private fun resolveMapLabel(mapId: String): String {
        return TagMaps.entries
            .firstOrNull { entry ->
                entry != TagMaps.RANDOM &&
                        runCatching { entry.mapProvider().mapId == mapId }.getOrDefault(false)
            }
            ?.name
            ?: TagMaps.RANDOM.name   // fallback
    }

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

    private fun heatColor(count: Int): String {
        val t = ((count - 1).coerceIn(0, 99) / 99.0)
        val r = (t * 255).toInt()
        val b = ((1.0 - t) * 255).toInt()
        return "#%02X%02X%02X".format(r, 0, b)
    }

    companion object {
        const val CLUSTER_RADIUS = 0.1
    }
}
