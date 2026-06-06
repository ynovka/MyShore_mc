package ru.ynovka.myShore.game.pillars.gameMode

import ru.ynovka.myShore.game.pillars.generators.platform.collectHexagonBlocks
import ru.ynovka.myShore.game.pillars.generators.platform.platformSideLength
import ru.ynovka.myShore.game.pillars.generators.platform.BlockPoint
import ru.ynovka.myShore.game.pillars.generators.platform.ChunkPoint
import ru.ynovka.myShore.game.pillars.generators.platform.PLATFORM_Y
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsGame
import java.util.concurrent.CompletableFuture
import ru.ynovka.myShore.game.GameState
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World


object LavaRushPillarsGM : PillarsGM {
    override fun roundStart(game: PillarsGame) {
        val state = game.fsm.current
        val world = game.gameWorld.get() ?: return
        fillLavaSchedule(
            game,
            state,
            world,
            PLATFORM_Y
        )
    }

    private fun fillLavaSchedule(
        game: PillarsGame,
        state: GameState<*, *, *>,
        world: World,
        lavaY: Int
    ) {
        // Заполняем текущий слой
        val sideLength = platformSideLength(game.gameWorld.pillars)
        val blocksByChunk = HashMap<ChunkPoint, MutableSet<BlockPoint>>()

        collectHexagonBlocks(sideLength, blocksByChunk)

        val futures = mutableListOf<CompletableFuture<Void>>()

        futures += blocksByChunk.map { (chunk, blocks) ->
            fillLavaChunk(world, chunk, blocks, lavaY)
        }

        CompletableFuture.allOf(*futures.toTypedArray())
            .thenAccept {

                // Следующий слой через 1 секунду
                scheduler.schedule {
                    if (game.fsm.current !== state) return@schedule
                    fillLavaSchedule(
                        game,
                        state,
                        world,
                        lavaY + 1
                    )
                }
                    .global()
                    .after(3 * 20L, Clock.TICKS)
                    .once()
            }
    }

    private fun fillLavaChunk(
        world: World,
        chunk: ChunkPoint,
        blocks: Set<BlockPoint>,
        lavaY: Int
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
                        val block = world.getBlockAt(block.x, lavaY, block.z)

                        val box = block.boundingBox
                        val isFullBlock = box.widthX >= 0.99 && box.widthZ >= 0.99 && box.height >= 0.99

                        if (block.isReplaceable || !isFullBlock) {
                            block.setType(Material.LAVA, false)
                        }
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