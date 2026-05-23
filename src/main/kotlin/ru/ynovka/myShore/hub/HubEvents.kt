package ru.ynovka.myShore.hub

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.ynovka.myShore.text.actionBar.ActionBar
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.MyShore.Companion.scheduler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.ynovka.myShore.party.PartyManager
import ru.ynovka.myShore.game.GameManager
import org.bukkit.potion.PotionEffectType
import ru.ynovka.myShore.party.LeftReason
import ru.ynovka.myShore.hub.Hub.toHub
import org.bukkit.potion.PotionEffect
import org.bukkit.event.EventPriority
import org.bukkit.event.EventHandler
import ru.ynovka.myShore.MyShore
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Bukkit


object HubEvents : Listener {
    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
        scheduler.schedule {
            Hub.world.players.forEach { player ->
                if (player.gameMode == GameMode.CREATIVE) return@schedule
                val l = player.location.clone().apply { y = Hub.spawn.y }
                val distance = Hub.spawn.distance(l)

                if (player.y < 87) {
                    player.toHub()
                    return@schedule
                }

                val t = l.world.getHighestBlockAt(l).type
                if (player.location.y < 99 &&
                    t in listOf(Material.VOID_AIR,
                        Material.BLUE_STAINED_GLASS_PANE,
                        Material.CYAN_STAINED_GLASS_PANE)
                    ) {
                    player.addPotionEffect(PotionEffect(
                        PotionEffectType.LEVITATION, 8, 2, false, false, false,
                    ))
                }

                if (distance > 20) {
                    val vec = Hub.spawn.clone().add(0.0, 15.0, 0.0).toVector()
                        .subtract(player.location.toVector()).normalize().multiply(2.5)
                    player.velocity = player.velocity.add(vec)
                }
            }
        }
            .repeatEvery(2L, Clock.TICKS)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player

        for (other in Bukkit.getOnlinePlayers()) {
            player.hidePlayer(inst, other)
            other.hidePlayer(inst, player)
        }

        scheduler.schedule {
            val isConnected = MyShore.plasmo.isPlayerConnected(player)
            if (!isConnected) {
                player.sendMessage("Похоже у вас не установлен мод PlasmoVoice")
                player.sendMessage("Без него вы не сможете поиграть в некоторые из игр")
                player.sendMessage("Если считаете что произошла ошибка, попробуйте /vrc")
            }
        }
            .after(6 * 20L, Clock.TICKS)
            .once()

        player.toHub()

        TabController.updateAll()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerLeave(e: PlayerQuitEvent) {
        ActionBar.clear(e.player)
        GameManager.leave(e.player.uniqueId)
        PartyManager.leave(e.player, LeftReason.QUIT)
        scheduler.schedule {
            TabController.updateAll()
        }
            .after(5L, Clock.TICKS)
            .once()
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (!e.player.isInHubWorld() || e.player.gameMode == GameMode.CREATIVE) return
        e.isCancelled = true
    }

    @EventHandler
    fun onPlayerAttack(e: PrePlayerAttackEntityEvent) {
        if (!e.player.isInHubWorld() || e.player.gameMode == GameMode.CREATIVE) return
        e.isCancelled = true
    }

    private fun Player.isInHubWorld() = world.name == "hub"
}