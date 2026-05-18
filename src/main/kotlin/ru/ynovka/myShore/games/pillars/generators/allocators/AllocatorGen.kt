package ru.ynovka.myShore.games.pillars.generators.allocators

import ru.ynovka.myShore.games.pillars.PillarsGame
import ru.ynovka.myShore.games.pillars.Pillar
import java.util.UUID


interface AllocatorGen {
    fun generate(pGame: PillarsGame, playerId: UUID): Pillar
}

enum class AllocatorGenerator(val gen: AllocatorGen) {
    HONEY(HoneyAllocatorGen),
}