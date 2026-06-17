package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.game.pillars.generators.platform.PlatformGenerator
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
    var allocatorGen: AllocatorGenerator,
    var platformGen: PlatformGenerator
) : GameWorld() {
    override val name = "pillars_${UUID.randomUUID()}"

    val pillars: MutableSet<Pillar> = mutableSetOf()

    fun countdownPrepare(shouldContinue: () -> Boolean = { true }): CompletableFuture<Void> {
        pillars.clear()

        return getOrCreate()
            .thenCompose { world ->
                val chunks = world.loadedChunks.map { it.x to it.z }

                val futures = chunks.map { (chunkX, chunkZ) ->
                    val future = CompletableFuture<Void>()

                    scheduler.schedule {
                        try {
                            if (!shouldContinue()) {
                                future.complete(null)
                                return@schedule
                            }

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

    fun spawnPlayer(
        pGame: PillarsGame,
        pPlayer: PillarsPlayer,
        updateBorder: Boolean = true,
        generatePlatform: Boolean = true
    ): CompletableFuture<Void> {
        return getOrCreate().thenCompose { world ->
            removePlayerPillar(pPlayer.playerId, world).thenCompose {
                val pillar = allocatorGen.gen.generate(pGame, pPlayer.playerId)

                val generateFuture = pillarGen.gen.generate(world, pillar)
                val boxFuture = PillarsPlayerBox.create(world, pillar)

                CompletableFuture.allOf(generateFuture, boxFuture)
                    .thenCompose {
                        if (updateBorder) {
                            updateWorldBorder(world)
                                .thenCompose { teleportAndSetupPlayer(world, pillar, pPlayer) }
                        } else {
                            teleportAndSetupPlayer(world, pillar, pPlayer)
                        }
                    }
                    .thenCompose {
                        if (generatePlatform) {
                            platformGen.gen.generate(world, pillars.toList())
                        } else {
                            CompletableFuture.completedFuture<Void>(null)
                        }
                    }
            }
        }.whenComplete { _, throwable ->
            if (throwable != null) throwable.printStackTrace()
        }
    }

    fun spawnPlayers(pGame: PillarsGame): CompletableFuture<Void> {
        val players = pGame.activePlayers.toList()

        if (players.isEmpty()) {
            return CompletableFuture.completedFuture(null)
        }

        val futures = players.map { pPlayer ->
            val future = CompletableFuture<Void>()

            scheduler.schedule {
                if (pPlayer !in pGame.activePlayers) {
                    future.complete(null)
                    return@schedule
                }

                spawnPlayer(pGame, pPlayer, updateBorder = false, generatePlatform = false)
                    .whenComplete { _, throwable ->
                        if (throwable != null) {
                            future.completeExceptionally(throwable)
                        } else {
                            future.complete(null)
                        }
                    }
            }.global().once()

            future
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenCompose {
                val world = get()
                    ?: return@thenCompose CompletableFuture.completedFuture<Void>(null)

                updateWorldBorder(world)
                    .thenCompose { platformGen.gen.generate(world, pillars.toList()) }
            }
    }

    private fun removePPillar(
        playerId: UUID,
        world: World
    ): CompletableFuture<Boolean> {
        val pillar = pillars.firstOrNull { it.owner == playerId }
            ?: return CompletableFuture.completedFuture(false)

        pillars.remove(pillar)

        return CompletableFuture.allOf(
            pillarGen.gen.remove(world, pillar),
            clearPlayerBox(world, pillar)
        ).thenApply { true }
    }

    fun removePlayerPillar(
        playerId: UUID,
        world: World
    ): CompletableFuture<Void> {
        return removePPillar(playerId, world).thenCompose { removed ->
            if (!removed) return@thenCompose CompletableFuture.completedFuture<Void>(null)

            updateWorldBorder(world)
        }
    }

    private fun updateWorldBorder(
        world: World
    ): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()

        scheduler.schedule {
            try {
                world.worldBorder.size = allocatorGen.gen.borderSize(this)
                future.complete(null)
            } catch (throwable: Throwable) {
                future.completeExceptionally(throwable)
            }
        }.global().once()

        return future
    }

    private fun teleportAndSetupPlayer(
        world: World,
        pillar: Pillar,
        pPlayer: PillarsPlayer
    ): CompletableFuture<Void> {
        val player = pPlayer.asPlayer() ?: return CompletableFuture.completedFuture(null)

        val future = CompletableFuture<Void>()

        val teleportLocation = Location(
            world,
            pillar.x + 0.5,
            TELEPORT_Y,
            pillar.z + 0.5
        )


        scheduler.schedule {
            player.teleportAsync(teleportLocation).whenComplete { success, throwable ->
                if (throwable != null) {
                    future.completeExceptionally(throwable)
                    return@whenComplete
                }

                if (!success) {
                    future.complete(null)
                    return@whenComplete
                }

                scheduler.schedule {
                    player.restrictToBlock(true)
                    player.gameMode = GameMode.ADVENTURE
                    player.allowFlight = false
                    player.isFlying = false
                    player.isInvulnerable = false
                    player.isCollidable = true
                    player.canPickupItems = true
                    player.saturation = 10f
                    player.foodLevel = 20
                    player.fireTicks = 0
                    player.health = 20.0

                    pPlayer.updateLastKnownY(teleportLocation.y)

                    player.inventory.clear()
                    player.inventory.setItem(8, HubItems.hubTeleport.getStack(null))

                    player.clearActivePotionEffects()

                    future.complete(null)
                }.entity(player).once()
            }
        }.entity(player).after(10L, Clock.TICKS).once()


        return future
    }

    private fun clearPlayerBox(world: World, pillar: Pillar): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val blockLoc = Location(world, pillar.x.toDouble(), TELEPORT_Y - 1, pillar.z.toDouble())

        scheduler.schedule {
            try {
                val centerX = blockLoc.blockX
                val centerZ = blockLoc.blockZ

                for (x in centerX - 2..centerX + 2) {
                    for (z in centerZ - 2..centerZ + 2) {
                        for (y in 101..116) {
                            world.getBlockAt(x, y, z).type = Material.AIR
                        }
                    }
                }

                future.complete(null)
            } catch (throwable: Throwable) {
                future.completeExceptionally(throwable)
            }
        }.region(blockLoc).once()

        return future
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
