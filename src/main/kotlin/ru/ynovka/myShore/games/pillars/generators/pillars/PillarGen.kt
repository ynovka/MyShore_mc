package ru.ynovka.myShore.games.pillars.generators.pillars

import ru.ynovka.myShore.games.pillars.Pillar
import org.bukkit.World


interface PillarGen {
    fun generate(world: World, pillar: Pillar)
}

enum class PillarGenerator(val gen: PillarGen) {
    DEFAULT(DefaultPillarGen),
    VOID(VoidPillarGen)
}