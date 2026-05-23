package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.PillarsGame.Companion.currentPillarsGame
import ru.ynovka.myShore.game.pillars.Pillar.Companion.TOP_BLOCK
import ru.ynovka.myShore.game.pillars.states.PillarsInProgress
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.MyShore.Companion.scheduler
import io.papermc.paper.event.entity.EntityMoveEvent
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import ru.ynovka.myShore.game.SpectatorReason
import net.kyori.adventure.text.Component
import org.bukkit.event.EventPriority
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

        val game = e.player.uniqueId.currentPillarsGame() ?: return

        val player = e.player
        when (game.fsm.current) {
            is PillarsInProgress -> {
                game.movePlayerToSpectator(player, SpectatorReason.ELIMINATED)

                val msg = Component.translatable(
                    "msg.myshore.player.fall_death",
                    Component.text(player.name)
                )
                val toAnon = game.gamePlayers + game.spectatorPlayers
                toAnon.asPlayers().forEach {
                    scheduler.schedule {
                        it.sendMessage(msg)
                    }.entity(it).once()
                }
            }
            else -> {
                scheduler.schedule {
                    val pillar = game.gameWorld.pillars.firstOrNull { it.owner == player.uniqueId } ?: return@schedule
                    val world = game.gameWorld.get() ?: return@schedule
                    player.velocity = Vector()
                    player.fallDistance = 0f
                    player.teleportAsync(Location(
                        world, pillar.x + 0.5, TOP_BLOCK + 1.0, pillar.z + 0.5
                    ))
                }.entity(player).once()
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        println("onPlayerDeath 1")
        if (!e.player.world.isPillarsWorld()) return

        val game = e.player.uniqueId.currentPillarsGame() ?: return
        e.isCancelled = true

        when (game.fsm.current) {
            is PillarsInProgress -> {
                game.movePlayerToSpectator(e.player, SpectatorReason.ELIMINATED)

                val killer = e.damageSource.causingEntity?.name ?: return

                val msg = Component.translatable(
                    "msg.myshore.player.kill",
                    Component.text(killer),
                    Component.text(e.player.name)
                )
                val toAnon = game.gamePlayers + game.spectatorPlayers
                toAnon.asPlayers().forEach {
                    scheduler.schedule {
                        it.sendMessage(msg)
                    }.entity(it).once()
                }
            }
        }
    }

    private fun World.isPillarsWorld() = name.startsWith("myshore_pillars_")
}