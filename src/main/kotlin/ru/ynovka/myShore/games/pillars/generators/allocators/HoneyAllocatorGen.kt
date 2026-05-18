package ru.ynovka.myShore.games.pillars.generators.allocators

import ru.ynovka.myShore.games.pillars.PillarsGame
import org.bukkit.Location
import ru.ynovka.myShore.games.pillars.Footprints
import ru.ynovka.myShore.games.pillars.Pillar
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.sqrt


object HoneyAllocatorGen : AllocatorGen {

    override fun generate(pGame: PillarsGame, playerId: UUID): Pillar {
        val occupied = pGame.gameWorld.pillars
        val center = pGame.gameWorld.world.spawnLocation

        var pointCount = maxOf(1, pGame.gamePlayers.size)

        while (true) {
            val points = honeycombRingLocations(
                center = center,
                pointCount = pointCount,
                playerId = playerId
            )

            val footprint = Footprints.hexagon(computeHexFootprintRadius(points))

            val free = points
                .map { loc -> loc.copy(footprint = footprint) }
                .firstOrNull { loc -> loc !in occupied }

            if (free != null) {
                occupied += free
                return free
            }

            pointCount++
        }
    }

    private fun computeHexFootprintRadius(points: List<Pillar>): Int {
        if (points.size <= 1) return 0

        var minDistSq = Double.MAX_VALUE

        for (i in points.indices) {
            for (j in i + 1 until points.size) {
                val dx = points[i].x - points[j].x
                val dz = points[i].z - points[j].z
                val dSq = (dx * dx + dz * dz).toDouble()

                if (dSq < minDistSq) {
                    minDistSq = dSq
                }
            }
        }

        val minDist = sqrt(minDistSq)

        return maxOf(0, (minDist / 2).toInt())
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
    playerId: UUID
): List<Pillar> {
    require(pointCount >= 1) { "pointCount must be at least 1" }

    return honeycombRingPoints(pointCount).map { point ->
        Pillar(
            x = center.blockX + point.x,
            z = center.blockZ + point.z,
            owner = playerId
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