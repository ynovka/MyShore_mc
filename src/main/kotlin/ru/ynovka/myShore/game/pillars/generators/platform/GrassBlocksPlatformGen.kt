package ru.ynovka.myShore.game.pillars.generators.platform

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.Pillar
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import java.util.concurrent.CompletableFuture
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor


object GrassBlocksPlatformGen : PlatformGen {

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

        futures += generateLogs(world, pillars)

        return CompletableFuture.allOf(*futures.toTypedArray())
    }

    private fun generateLogs(
        world: World,
        pillars: Collection<Pillar>
    ): CompletableFuture<Void> {

        val future = CompletableFuture<Void>()

        val first = pillars.first()
        val origin = Location(world, first.x.toDouble(), Pillar.TOP_BLOCK.toDouble(), first.z.toDouble())

        scheduler.schedule {
            try {
                for (pillar in pillars) {
                    for (offsetX in -1..1) {
                        for (offsetZ in -1..1) {
                            if (offsetX == 0 && offsetZ == 0) continue

                            world.getBlockAt(
                                pillar.x + offsetX,
                                Pillar.TOP_BLOCK - 64,
                                pillar.z + offsetZ
                            ).setType(Material.OAK_LOG, false)
                        }
                    }
                }

                future.complete(null)
            } catch (t: Throwable) {
                future.completeExceptionally(t)
            }
        }.region(origin).once()

        return future
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
                    world.getBlockAt(block.x, PLATFORM_Y, block.z)
                        .setType(Material.GRASS_BLOCK, false)
                }

                future.complete(null)
            } catch (throwable: Throwable) {
                future.completeExceptionally(throwable)
            }
        }.region(origin).once()

        return future
    }
}
