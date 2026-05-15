package ru.ynovka.myShore.games.pillars.generators.allocators

import ru.ynovka.myShore.games.pillars.PillarsGame


interface AllocatorGen {
    fun generate(pGame: PillarsGame)
}

enum class AllocatorGenerator(val gen: AllocatorGen) {
    HONEY(HoneyAllocatorGen),
}