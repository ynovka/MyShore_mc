package ru.ynovka.myShore.game.pillars.generators.allocators

import ru.ynovka.myShore.game.pillars.Pillar.Companion.BORDER_PADDING
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.pillars.Pillar
import org.bukkit.Location
import java.util.UUID
import kotlin.math.*


object RingAllocatorGen : AllocatorGen {

    private const val MIN_DISTANCE = 8.5
    private const val START_RADIUS = 8.5
    private const val RADIUS_STEP = 8.5

    override fun generate(pGame: PillarsGame, playerId: UUID): Pillar {
        val origin = pGame.gameWorld.getOrCreate().get().spawnLocation
        val occupied = pGame.gameWorld.pillars

        val point = ringPoints(origin)
            .first { point ->
                occupied.none { pillar ->
                    pillar.x == point.x && pillar.z == point.z
                }
            }

        return Pillar(
            x = point.x,
            z = point.z,
            owner = playerId
        ).also { occupied += it }
    }

    override fun borderSize(pWorld: PillarsWorld): Double {
        val origin = pWorld.getOrCreate().get().spawnLocation
        val pillars = pWorld.pillars

        if (pillars.isEmpty()) {
            return BORDER_PADDING * 2.0
        }

        val maxDistance = pillars.maxOf { pillar ->
            maxOf(
                abs(pillar.x - origin.blockX),
                abs(pillar.z - origin.blockZ)
            )
        }

        return maxDistance * 2.0 + BORDER_PADDING * 2.0
    }


    private fun ringPoints(origin: Location): Sequence<IntPoint> = sequence {
        var radius = START_RADIUS

        while (true) {
            for (point in pointsOnRing(origin, radius)) {
                yield(point)
            }

            radius += RADIUS_STEP
        }
    }

    private fun pointsOnRing(origin: Location, radius: Double): List<IntPoint> {
        val circumference = 2.0 * PI * radius

        val pointsCount = max(
            1,
            floor(circumference / MIN_DISTANCE).toInt()
        )

        return buildList {
            for (i in 0 until pointsCount) {
                val angle = 2.0 * PI * i / pointsCount

                add(
                    IntPoint(
                        x = origin.blockX + (cos(angle) * radius).roundToInt(),
                        z = origin.blockZ + (sin(angle) * radius).roundToInt()
                    )
                )
            }
        }.distinct()
    }

    private data class IntPoint(
        val x: Int,
        val z: Int
    )
}