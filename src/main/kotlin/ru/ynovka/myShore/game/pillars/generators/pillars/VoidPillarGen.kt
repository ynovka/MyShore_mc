package ru.ynovka.myShore.game.pillars.generators.pillars

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.Pillar
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World


object VoidPillarGen : PillarGen {
    override fun generate(world: World, pillar: Pillar) {
        val origin = Location(world, pillar.x.toDouble(), Pillar.TOP_BLOCK.toDouble(), pillar.z.toDouble())

        scheduler.schedule {
            for (y in (Pillar.TOP_BLOCK - 5)..Pillar.TOP_BLOCK) {
                world.getBlockAt(pillar.x, y, pillar.z).type = Material.BEDROCK
            }
        }.region(origin).once()
    }
}