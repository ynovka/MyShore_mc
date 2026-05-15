package ru.ynovka.myShore.games.pillars.generators.pillars

import org.bukkit.Material
import org.bukkit.World
import ru.ynovka.myShore.games.pillars.PillarLoc


object SlimePillarGen : PillarGen {
    override fun generate(world: World, pillar: PillarLoc) {
        for (y in (PillarLoc.TOP_BLOCK - 64)..PillarLoc.TOP_BLOCK) {
            world.getBlockAt(pillar.x, y, pillar.z).type = Material.BEDROCK
        }

        for ((dx, dz) in pillar.footprint) {
            world.getBlockAt(pillar.x + dx, PillarLoc.TOP_BLOCK, pillar.z + dz).type = Material.SLIME_BLOCK
        }
    }
}