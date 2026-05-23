package ru.ynovka.myShore.game.pillars.generators.pillars

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.Pillar
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World


object SlimePillarGen : PillarGen {
    override fun generate(world: World, pillar: Pillar) {
        val origin = Location(world, pillar.x.toDouble(), Pillar.TOP_BLOCK.toDouble(), pillar.z.toDouble())

        scheduler.schedule {
            for (y in (Pillar.TOP_BLOCK - 64)..Pillar.TOP_BLOCK) {
                world.getBlockAt(pillar.x, y, pillar.z).type = Material.BEDROCK
            }

            for ((dx, dz) in pillar.footprint) {
                world.getBlockAt(
                    pillar.x + dx,
                    Pillar.TOP_BLOCK,
                    pillar.z + dz
                ).type = Material.SLIME_BLOCK
            }
        }.region(origin).once()
    }
}