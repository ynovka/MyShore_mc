package ru.ynovka.myShore.game.pillars.generators.pillars

import ru.ynovka.myShore.game.pillars.Pillar
import org.bukkit.World


interface PillarGen {
    fun generate(world: World, pillar: Pillar)
}

enum class PillarGenerator(val gen: PillarGen) {
    DEFAULT(DefaultPillarGen),
    VOID(VoidPillarGen)
}