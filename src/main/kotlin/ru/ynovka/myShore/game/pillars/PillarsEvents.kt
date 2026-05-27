package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.PillarsGame.Companion.currentPillarsGame
import ru.ynovka.myShore.game.GamePlayer.Companion.forEachOnlinePlayer
import ru.ynovka.myShore.game.pillars.Pillar.Companion.TOP_BLOCK
import ru.ynovka.myShore.game.pillars.states.PillarsInProgress
import ru.ynovka.myShore.game.gameUtils.PlayerDeathMessages
import ru.ynovka.myShore.MyShore.Companion.scheduler
import io.papermc.paper.event.entity.EntityMoveEvent
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import ru.ynovka.myShore.game.SpectatorReason
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World


object PillarsEvents : Listener {

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler
    fun onEntityMove(e: EntityMoveEvent) {
        val entity = e.entity
        if (entity is Player) return
        scheduler.schedule {
            if (entity.location.y < 0.0) entity.remove()
        }.entity(entity).once()
    }

    @EventHandler
    fun onPlayerFall(e: PlayerMoveEvent) {
        if (!e.player.world.isPillarsWorld()) return
        if (e.player.gameMode != GameMode.SURVIVAL) return
        if (e.to.y > 0.0) return
        if (e.from.y <= 0.0) return

        val game = e.player.uniqueId.currentPillarsGame() ?: return
        val pPlayer = game.getOrCreatePlayer(e.player.uniqueId)
        when (game.fsm.current) {
            is PillarsInProgress -> {
                game.movePlayerToSpectator(pPlayer, SpectatorReason.ELIMINATED)
                pPlayer.withOnlinePlayer { player ->
                    player.gameMode = GameMode.SPECTATOR
                    game.gameWorld.get()?.let { world ->
                        player.teleportAsync(Location(world, 0.0, 110.0, 0.0))
                    }
                    game.broadcast(PlayerDeathMessages.voidFall(player))
                }
            }
            else -> {
                val pillar = game.gameWorld.pillars.firstOrNull { it.owner == pPlayer.playerId } ?: return
                val world = game.gameWorld.get() ?: return
                pPlayer.withOnlinePlayer { player ->
                    scheduler.schedule {
                        player.velocity = Vector()
                        player.fallDistance = 0f
                        player.teleportAsync(Location(
                            world, pillar.x + 0.5, TOP_BLOCK + 1.0, pillar.z + 0.5
                        ))
                    }.entity(player).once()
                }
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val player = e.player

        if (!player.world.isPillarsWorld()) return

        val game = player.uniqueId.currentPillarsGame() ?: return
        val pPlayer = game.getOrCreatePlayer(player.uniqueId)

        val drops = e.drops
            .filter { !it.type.isAir && it.amount > 0 }
            .map { it.clone() }

        e.isCancelled = true

        e.drops.clear()
        e.droppedExp = 0

        when (game.fsm.current) {
            is PillarsInProgress -> {
                dropDeathItems(player, drops)

                game.movePlayerToSpectator(pPlayer, SpectatorReason.ELIMINATED)
                game.broadcast(PlayerDeathMessages.from(e))
            }
        }
    }

    private fun dropDeathItems(player: Player, drops: List<ItemStack>) {
        val world = player.world

        val dropLocation = player.location.clone()
            .add(0.0, player.eyeHeight - 0.3, 0.0)

        for (stack in drops) {
            if (stack.type.isAir || stack.amount <= 0) continue

            world.dropItem(dropLocation, stack.clone()) { item ->
                item.pickupDelay = 10
                item.velocity = randomDeathDropVelocity()
            }
        }
    }

    private fun randomDeathDropVelocity(): Vector {
        val angle = kotlin.random.Random.nextDouble() * Math.PI * 2.0
        val speed = kotlin.random.Random.nextDouble() * 0.5

        return Vector(
            -kotlin.math.sin(angle) * speed,
            0.2,
            kotlin.math.cos(angle) * speed
        )
    }

    @EventHandler
    fun onPlayerKill(e: PlayerDeathEvent) {
        if (!e.player.world.isPillarsWorld()) return

        val game = e.player.uniqueId.currentPillarsGame() ?: return
        if (game.fsm.current !is PillarsInProgress) return

        val killerId = e.entity.killer?.uniqueId ?: return
        val killer = game.getOrCreatePlayer(killerId)

        killer.addKill()
    }

    private fun PillarsGame.broadcast(message: Component) {
        val viewers = activePlayers + spectatorPlayers
        viewers.forEachOnlinePlayer { player ->
            scheduler.schedule {
                player.sendMessage(message)
            }.entity(player).once()
        }
    }

    private fun World.isPillarsWorld() = name.startsWith("myshore_pillars_")
}