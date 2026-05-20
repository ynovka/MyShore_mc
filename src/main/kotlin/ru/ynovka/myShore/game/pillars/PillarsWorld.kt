package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.utils.InstantChunkClear
import ru.ynovka.myShore.MyShore.Companion.inst
import java.util.concurrent.CompletableFuture
import ru.ynovka.myShore.game.GameWorld
import org.bukkit.Location
import org.bukkit.Bukkit
import ru.ynovka.myShore.game.pillars.Pillar.Companion.TELEPORT_Y
import java.util.UUID


class PillarsWorld : GameWorld() {
    override val name = "pillars_${UUID.randomUUID()}"

    val pillars: MutableSet<Pillar> = mutableSetOf()

    fun countdownPrepare() {
        getOrCreate().thenCompose { world ->
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
    }


    fun spawnPlayer(pGame: PillarsGame, pPlayer: PillarsPlayer) {
        getOrCreate().thenCompose { world ->
            // Ищём свободное место для спавна столба
            val loc = pGame.allocator.gen.generate(pGame, pPlayer.playerId)
            // Создаём столб на нужных координтах
            pGame.pillar.gen.generate(world, loc)
            // Создаём коробку игрока
            PillarsPlayerBox.create(world, loc)
            // телепортируем игрока в коробку
            pPlayer.player.teleportAsync(
                Location(
                    world,
                    loc.x + 0.5, TELEPORT_Y, loc.z + 0.5
                )
            )
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
