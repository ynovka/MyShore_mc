package ru.ynovka.myShore.game.pillars.generators.platform

import ru.ynovka.myShore.MyShore.Companion.scheduler
import java.util.concurrent.CompletableFuture
import ru.ynovka.myShore.game.pillars.Pillar
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World


object SlimeAndEmeraldBlocksPlatformGen : PlatformGen {

    override fun generate(
        world: World,
        pillars: Collection<Pillar>
    ): CompletableFuture<Void> {

        if (pillars.isEmpty()) {
            return CompletableFuture.completedFuture(null)
        }

        val sideLength = platformSideLength(pillars)
        val blocksByChunk = HashMap<ChunkPoint, MutableSet<BlockPoint>>()

        collectHexagonBlocks(sideLength, blocksByChunk)

        val futures = mutableListOf<CompletableFuture<Void>>()

        futures += blocksByChunk.map { (chunk, blocks) ->
            generateChunk(world, chunk, blocks)
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
    }

    private fun generateChunk(
        world: World,
        chunk: ChunkPoint,
        blocks: Set<BlockPoint>
    ): CompletableFuture<Void> {
        val origin = Location(
            world,
            chunk.x * 16.0 + 8.0,
            PLATFORM_Y.toDouble(),
            chunk.z * 16.0 + 8.0
        )
        val future = CompletableFuture<Void>()

        scheduler.schedule {
            try {
                for (block in blocks) {
                    if (Random.nextBoolean()) {
                        world.getBlockAt(block.x, PLATFORM_Y, block.z)
                            .setType(Material.EMERALD_BLOCK, false)
                    } else {
                        world.getBlockAt(block.x, PLATFORM_Y, block.z)
                            .setType(Material.SLIME_BLOCK, false)
                    }
                }

                future.complete(null)
            } catch (throwable: Throwable) {
                future.completeExceptionally(throwable)
            }
        }.region(origin).once()

        return future
    }
}
