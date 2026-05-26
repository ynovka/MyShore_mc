package ru.ynovka.myShore.game.pillars.generators.allocators

import ru.ynovka.myShore.game.pillars.Pillar.Companion.BORDER_PADDING
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.pillars.Pillar
import kotlin.math.abs
import java.util.UUID


object HoneyAllocatorGen : AllocatorGen {

    private const val STEP_X = 9
    private const val STEP_Z = 8
    private const val OFFSET_X = 4

    private const val CENTER_X = 0
    private const val CENTER_Z = 0

    override fun generate(pGame: PillarsGame, playerId: UUID): Pillar {
        val occupied = pGame.gameWorld.pillars

        val point = honeycombPoints()
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
        val pillars = pWorld.pillars

        if (pillars.isEmpty()) {
            return BORDER_PADDING * 2.0
        }

        val maxDistance = pillars.maxOf { pillar ->
            maxOf(
                abs(pillar.x - CENTER_X),
                abs(pillar.z - CENTER_Z)
            )
        }

        return maxDistance * 2.0 + BORDER_PADDING * 2.0
    }

    private fun honeycombPoints(): Sequence<IntPoint> = sequence {
        var radius = 1

        while (true) {
            for (cell in hexRingSymmetric(radius)) {
                yield(
                    IntPoint(
                        x = CENTER_X + cell.q * STEP_X + cell.r * OFFSET_X,
                        z = CENTER_Z + cell.r * STEP_Z
                    )
                )
            }

            radius++
        }
    }

    private fun hexRingSymmetric(radius: Int): List<HexCell> {
        val ring = hexRing(radius)
        val used = HashSet<HexCell>()
        val result = ArrayList<HexCell>(ring.size)

        for (cell in ring) {
            if (!used.add(cell)) continue

            result += cell

            val opposite = HexCell(
                q = -cell.q,
                r = -cell.r
            )

            if (used.add(opposite)) {
                result += opposite
            }
        }

        return result
    }

    private fun hexRing(radius: Int): List<HexCell> {
        return buildList {
            for (q in -radius..radius) {
                for (r in -radius..radius) {
                    if (hexDistance(q, r) == radius) {
                        add(HexCell(q, r))
                    }
                }
            }
        }
    }

    private fun hexDistance(q: Int, r: Int): Int {
        val s = -q - r
        return maxOf(abs(q), abs(r), abs(s))
    }

    private data class HexCell(
        val q: Int,
        val r: Int
    )

    private data class IntPoint(
        val x: Int,
        val z: Int
    )
}