package ru.ynovka.myShore.games.pillars.generators.pillars

import ru.ynovka.myShore.games.pillars.Pillar
import org.bukkit.Material
import org.bukkit.World


object VoidPillarGen : PillarGen {
    override fun generate(world: World, pillar: Pillar) {
        val minY = 100 - 4
        val maxY = 100

        for (y in minY..maxY) {
            val block = world.getBlockAt(pillar.x, y, pillar.z)
            block.type = Material.BEDROCK
        }
    }
}