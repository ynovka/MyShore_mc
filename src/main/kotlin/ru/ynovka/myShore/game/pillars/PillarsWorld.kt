package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.Pillar.Companion.TELEPORT_Y
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.utils.InstantChunkClear
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.restrictToBlock
import java.util.concurrent.CompletableFuture
import ru.ynovka.myShore.game.GameWorld
import org.bukkit.Location
import org.bukkit.Bukkit
import java.util.UUID


class PillarsWorld : GameWorld() {
    override val name = "pillars_${UUID.randomUUID()}"

    val pillars: MutableSet<Pillar> = mutableSetOf()

    fun countdownPrepare(): CompletableFuture<Void> {
        return getOrCreate()
            .thenCompose { world ->
                val chunks = world.loadedChunks.map { it.x to it.z }

                val futures = chunks.map { (chunkX, chunkZ) ->
                    val future = CompletableFuture<Void>()

                    scheduler.schedule {
                        try {
                            if (!Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
                                inst.logger.warning(
                                    "[InstantChunkClear] Chunk $chunkX,$chunkZ is not owned by current region"
                                )
                                future.complete(null)
                                return@schedule
                            }

                            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                                future.complete(null)
                                return@schedule
                            }

                            val chunk = world.getChunkAt(chunkX, chunkZ)
                            InstantChunkClear.clearChunk(chunk)

                            future.complete(null)
                        } catch (throwable: Throwable) {
                            future.completeExceptionally(throwable)
                        }
                    }
                        .region(Location(world, chunkX * 16.0 + 8.0, 0.0, chunkZ * 16.0 + 8.0))
                        .once()

                    future
                }

                CompletableFuture.allOf(*futures.toTypedArray())
            }
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    throwable.printStackTrace()
                }
            }
    }

    fun spawnPlayer(pGame: PillarsGame, pPlayer: PillarsPlayer): CompletableFuture<Void> {
        return getOrCreate().thenCompose { world ->
            val loc = pGame.allocator.gen.generate(pGame, pPlayer.playerId)

            pGame.pillar.gen.generate(world, loc)
            PillarsPlayerBox.create(world, loc)

            val teleportLocation = Location(
                world,
                loc.x + 0.5,
                TELEPORT_Y,
                loc.z + 0.5
            )

            pPlayer.player.teleportAsync(teleportLocation).thenAccept { success ->
                if (!success) return@thenAccept

                pPlayer.player.restrictToBlock(true)
            }
        }.whenComplete { _, throwable ->
            if (throwable != null) throwable.printStackTrace()
        }
    }

    fun spawnPlayers(pGame: PillarsGame) = pGame.gamePlayers.forEach { spawnPlayer(pGame, it) }
}

data class Pillar(
    val x: Int,
    val z: Int,
    val owner: UUID,
    val footprint: Footprint = Footprints.single
) {
    companion object {
        const val TOP_BLOCK = 100
        const val TELEPORT_Y = 110.0
    }
}
