package ru.ynovka.myShore.game.pillars.generators.pillars

import org.bukkit.Material
import org.bukkit.World
import ru.ynovka.myShore.game.pillars.Pillar


object SlimePillarGen : PillarGen {
    override fun generate(world: World, pillar: Pillar) {
        for (y in (Pillar.TOP_BLOCK - 64)..Pillar.TOP_BLOCK) {
            world.getBlockAt(pillar.x, y, pillar.z).type = Material.BEDROCK
        }

        for ((dx, dz) in pillar.footprint) {
            world.getBlockAt(pillar.x + dx, Pillar.TOP_BLOCK, pillar.z + dz).type = Material.SLIME_BLOCK
        }
    }
}