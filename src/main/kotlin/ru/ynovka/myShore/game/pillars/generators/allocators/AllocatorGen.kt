package ru.ynovka.myShore.game.pillars.generators.allocators

import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.pillars.Pillar
import java.util.UUID


interface AllocatorGen {
    fun generate(pGame: PillarsGame, playerId: UUID): Pillar
    fun borderSize(pWorld: PillarsWorld): Double
}

enum class AllocatorGenerator(val gen: AllocatorGen) {
    HONEY(HoneyAllocatorGen),
    RING(RingAllocatorGen),
}