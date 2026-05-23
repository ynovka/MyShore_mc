package ru.ynovka.myShore.game.pillars.generators.allocators

import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.pillars.Pillar
import java.util.UUID


interface AllocatorGen {
    fun generate(pGame: PillarsGame, playerId: UUID): Pillar
}

enum class AllocatorGenerator(val gen: AllocatorGen) {
    HONEY(HoneyAllocatorGen),
}