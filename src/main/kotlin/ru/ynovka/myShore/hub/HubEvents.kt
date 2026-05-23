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
import org.bukkit.event.entity.PlayerDeathEvent


object HubEvents : Listener {
    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        println("onPlayerDeath 2")
        if (e.isCancelled) return
        println("onPlayerDeath 22")
        e.isCancelled = true
        e.player.toHub()
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