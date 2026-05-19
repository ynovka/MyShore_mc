package ru.ynovka.myShore.game.pillars.generators.pillars

import ru.ynovka.myShore.game.pillars.Pillar
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