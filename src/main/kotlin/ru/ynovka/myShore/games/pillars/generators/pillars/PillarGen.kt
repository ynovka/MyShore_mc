package ru.ynovka.myShore.games.pillars.generators.pillars

import org.bukkit.World
import ru.ynovka.myShore.games.pillars.PillarsWorld
import ru.ynovka.myShore.games.pillars.PillarLoc


interface PillarGen {
    fun generate(pWorld: PillarsWorld) {
        val world = pWorld.world
        pWorld.pillars.forEach { generate(world, it) }
    }
    fun generate(world: World, pillar: PillarLoc)
}

enum class PillarGenerator(val gen: PillarGen) {
    DEFAULT(DefaultPillarGen),
    VOID(VoidPillarGen)
}