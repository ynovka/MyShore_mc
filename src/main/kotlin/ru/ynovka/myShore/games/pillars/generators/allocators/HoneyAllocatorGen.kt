package ru.ynovka.myShore.games.pillars.generators.allocators

import ru.ynovka.myShore.games.pillars.PillarsGame
import org.bukkit.Location
import ru.ynovka.myShore.games.pillars.Footprints
import ru.ynovka.myShore.games.pillars.PillarLoc
import kotlin.math.atan2
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.sqrt


object HoneyAllocatorGen : AllocatorGen {
    override fun generate(pGame: PillarsGame) {
        val points = honeycombRingLocations(
            center = pGame.gameWorld.world.spawnLocation,
            pointCount = pGame.gamePlayers.size,
            y = 100.0
        )

        val footprint = Footprints.hexagon(computeHexFootprintRadius(points))

        points.forEach { loc ->
            pGame.gameWorld.pillars += PillarLoc(
                x = loc.blockX,
                z = loc.blockZ,
                footprint = footprint
            )
        }
    }

    /** Вычисляет радиус footprint из минимального расстояния между точками, оставляя 1 блок зазора */
    private fun computeHexFootprintRadius(points: List<Location>): Int {
        if (points.size <= 1) return 0
        var minDistSq = Double.MAX_VALUE
        for (i in points.indices) {
            for (j in i + 1 until points.size) {
                val dx = points[i].blockX - points[j].blockX
                val dz = points[i].blockZ - points[j].blockZ
                val dSq = (dx * dx + dz * dz).toDouble()
                if (dSq < minDistSq) minDistSq = dSq
            }
        }
        val minDist = sqrt(minDistSq)
        return (minDist / 2).toInt()
    }
}

private const val MIN_DISTANCE = 8.5
private const val MIN_DISTANCE_SQ = MIN_DISTANCE * MIN_DISTANCE

private data class HexCell(
    val q: Int,
    val r: Int
)

private data class IntPoint(
    val x: Int,
    val z: Int
)

private fun honeycombRingLocations(
    center: Location,
    pointCount: Int,
    y: Double = 100.0
): List<Location> {
    require(pointCount >= 1) { "pointCount must be at least 1" }

    return honeycombRingPoints(pointCount).map { point ->
        Location(
            center.world,
            (center.blockX + point.x).toDouble(),
            y,
            (center.blockZ + point.z).toDouble(),
            center.yaw,
            center.pitch
        )
    }
}

private fun honeycombRingPoints(count: Int): List<IntPoint> {
    if (count <= 1) return listOf(IntPoint(0, 0))

    val selected = mutableListOf<IntPoint>()
    var ring = 0

    while (selected.size < count && ring < 40) {
        val cells = balancedPickOrder(hexRingCells(ring))

        for (cell in cells) {
            if (selected.size >= count) break

            val point = hexToWorld(cell.q, cell.r)

            if (canAdd(selected, point)) {
                selected += point
            }
        }

        ring++
    }

    require(selected.size == count) {
        "Could only place ${selected.size} points. Increase max ring limit."
    }

    return selected
}

private fun hexToWorld(q: Int, r: Int): IntPoint {
    return IntPoint(
        x = q * 9 + r * 4,
        z = r * 8
    )
}

private fun hexDistance(q: Int, r: Int): Int {
    val s = -q - r
    return maxOf(abs(q), abs(r), abs(s))
}

private fun hexRingCells(radius: Int): List<HexCell> {
    if (radius == 0) return listOf(HexCell(0, 0))

    val cells = mutableListOf<HexCell>()

    for (q in -radius..radius) {
        for (r in -radius..radius) {
            if (hexDistance(q, r) == radius) {
                cells += HexCell(q, r)
            }
        }
    }

    return cells.sortedBy { cell ->
        val point = hexToWorld(cell.q, cell.r)
        atan2(point.z.toDouble(), point.x.toDouble())
    }
}

private fun balancedPickOrder(items: List<HexCell>): List<HexCell> {
    if (items.size <= 2) return items

    val selected = mutableListOf<HexCell>()
    val remaining = items.toMutableList()

    while (remaining.isNotEmpty()) {
        var bestIndex = 0
        var bestScore = Double.NEGATIVE_INFINITY

        for (i in remaining.indices) {
            val candidate = remaining[i]
            val candidateAngle = angleOf(candidate)

            val score =
                if (selected.isEmpty()) {
                    0.0
                } else {
                    selected.minOf { existing ->
                        angularDistance(candidateAngle, angleOf(existing))
                    }
                }

            if (score > bestScore) {
                bestScore = score
                bestIndex = i
            }
        }

        selected += remaining.removeAt(bestIndex)
    }

    return selected
}

private fun angleOf(cell: HexCell): Double {
    val point = hexToWorld(cell.q, cell.r)
    return atan2(point.z.toDouble(), point.x.toDouble())
}

private fun angularDistance(a: Double, b: Double): Double {
    val diff = abs(a - b)
    return minOf(diff, 2.0 * PI - diff)
}

private fun canAdd(points: List<IntPoint>, candidate: IntPoint): Boolean {
    return points.all { point ->
        val dx = candidate.x - point.x
        val dz = candidate.z - point.z
        dx * dx + dz * dz >= MIN_DISTANCE_SQ
    }
}