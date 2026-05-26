package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.game.pillars.Pillar.Companion.TELEPORT_Y
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.utils.InstantChunkClear
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.restrictToBlock
import java.util.concurrent.CompletableFuture
import ru.ynovka.myShore.game.GameWorld
import ru.ynovka.myShore.hub.HubItems
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.GameMode
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.UUID


class PillarsWorld(
    var pillarGen: PillarGenerator,
    var allocatorGen: AllocatorGenerator
) : GameWorld() {
    override val name = "pillars_${UUID.randomUUID()}"

    val pillars: MutableSet<Pillar> = mutableSetOf()

    fun countdownPrepare(): CompletableFuture<Void> {
        pillars.clear()

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
            removePlayerPillar(pPlayer.playerId, world)

            val pillar = allocatorGen.gen.generate(pGame, pPlayer.playerId)

            pillarGen.gen.generate(world, pillar)
            PillarsPlayerBox.create(world, pillar)

            scheduler.schedule {
                world.worldBorder.size = allocatorGen.gen.borderSize(this)
            }.global().once()

            val teleportLocation = Location(
                world,
                pillar.x + 0.5,
                TELEPORT_Y,
                pillar.z + 0.5
            )

            pPlayer.withOnlinePlayer { player ->
                player.teleportAsync(teleportLocation).thenAccept { success ->
                    if (!success) return@thenAccept

                    scheduler.schedule {
                        player.restrictToBlock(true)
                        player.gameMode = GameMode.ADVENTURE
                        player.foodLevel = 20
                        player.saturation = 10f
                        player.health = 20.0
                        player.inventory.clear()
                        player.inventory.setItem(8, HubItems.hubTeleport.getStack(null))
                        player.activePotionEffects.clear()
                    }.entity(player).after(20, Clock.TICKS).once()
                }
            }

            return@thenCompose CompletableFuture.completedFuture<Void>(null)
        }.whenComplete { _, throwable ->
            if (throwable != null) throwable.printStackTrace()
        }
    }

    fun spawnPlayers(pGame: PillarsGame) {
        pGame.activePlayers.forEach { spawnPlayer(pGame, it) }
    }

    private fun removePPillar(
        playerId: UUID,
        world: World
    ): Boolean {
        val pillar = pillars.firstOrNull { it.owner == playerId } ?: return false

        pillarGen.gen.remove(world, pillar)


        val blockLoc = Location(world, pillar.x.toDouble(), TELEPORT_Y - 1, pillar.z.toDouble())

        scheduler.schedule {
            val centerX = blockLoc.blockX
            val centerZ = blockLoc.blockZ

            for (x in centerX - 2..centerX + 2) {
                for (z in centerZ - 2..centerZ + 2) {
                    for (y in 101..116) {
                        world.getBlockAt(x, y, z).type = Material.AIR
                    }
                }
            }
        }.region(blockLoc).once()

        pillars.remove(pillar)
        return true
    }

    fun removePlayerPillar(
        playerId: UUID,
        world: World
    ): CompletableFuture<Void> {
        val removed = removePPillar(playerId, world)

        if (!removed) {
            return CompletableFuture.completedFuture(null)
        }

        return getOrCreate().thenAccept { world ->
            world.worldBorder.size = allocatorGen.gen.borderSize(this)
            world.worldBorder.center = Location(world, 0.0001, 0.0, 0.0001)
        }
    }
}

data class Pillar(
    val x: Int,
    val z: Int,
    val owner: UUID
) {
    companion object {
        const val TOP_BLOCK = 100
        const val TELEPORT_Y = 105.0
        const val BORDER_PADDING = 16.0
    }
}