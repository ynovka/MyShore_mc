package ru.ynovka.myShore.game.pillars.generators.pillars

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.Pillar
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import java.util.concurrent.CompletableFuture


object DefaultPillarGen : PillarGen {
    override fun generate(world: World, pillar: Pillar): CompletableFuture<Void> {
        val origin = Location(world, pillar.x.toDouble(), Pillar.TOP_BLOCK.toDouble(), pillar.z.toDouble())
        val future = CompletableFuture<Void>()

        scheduler.schedule {
            try {
                for (y in (Pillar.TOP_BLOCK - 64)..Pillar.TOP_BLOCK) {
                    world.getBlockAt(pillar.x, y, pillar.z).setType(Material.BEDROCK, false)
                }
                future.complete(null)
            } catch (throwable: Throwable) {
                future.completeExceptionally(throwable)
            }
        }.region(origin).once()

        return future
    }

    override fun remove(world: World, pillar: Pillar): CompletableFuture<Void> {
        val origin = Location(world, pillar.x.toDouble(), Pillar.TOP_BLOCK.toDouble(), pillar.z.toDouble())
        val future = CompletableFuture<Void>()

        scheduler.schedule {
            try {
                for (y in (Pillar.TOP_BLOCK - 64)..Pillar.TOP_BLOCK) {
                    world.getBlockAt(pillar.x, y, pillar.z).setType(Material.AIR, false)
                }
                future.complete(null)
            } catch (throwable: Throwable) {
                future.completeExceptionally(throwable)
            }
        }.region(origin).once()

        return future
    }
}
