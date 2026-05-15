package ru.ynovka.myShore.games.pillars.generators.pillars

import org.bukkit.Material
import org.bukkit.World
import ru.ynovka.myShore.games.pillars.PillarLoc


object DefaultPillarGen : PillarGen {
    override fun generate(world: World, pillar: PillarLoc) {
        val minY = 100 - 64
        val maxY = 100

        for (y in minY..maxY) {
            val block = world.getBlockAt(pillar.x, y, pillar.z)
            block.type = Material.BEDROCK
        }
    }
}