package ru.ynovka.myShore.hub

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import net.kyori.adventure.text.Component
import org.bukkit.potion.PotionEffectType
import ru.ynovka.myShore.hub.Hub.toHub
import org.bukkit.potion.PotionEffect
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.GameMode
import org.bukkit.Material


object HubEvents : Listener {
    fun regsiter() {
        inst.server.pluginManager.registerEvents(this, inst)
        inst.server.scheduler.runTaskTimer(inst, Runnable {
            Hub.world.players.forEach { player ->
                if (player.gameMode == GameMode.CREATIVE) return@Runnable
                val l = player.location.clone().apply { y = Hub.spawn.y }
                val distance = Hub.spawn.distance(l)

                if (player.y < 87) {
                    player.toHub()
                    return@Runnable
                }

                val t = l.world.getHighestBlockAt(l).type
                if (player.location.y < 99 &&
                    t in listOf(Material.VOID_AIR,
                        Material.BLUE_STAINED_GLASS_PANE,
                        Material.CYAN_STAINED_GLASS_PANE)
                    ) {
                    player.addPotionEffect(PotionEffect(
                        PotionEffectType.LEVITATION, 15, 2, false, false, false,
                    ))
                }

                if (distance > 20) {
                    val vec = Hub.spawn.clone().add(0.0, 15.0, 0.0).toVector()
                        .subtract(player.location.toVector()).normalize().multiply(2.5)
                    player.velocity = player.velocity.add(vec)
                }
            }
        }, 0L, 5L)
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player

        player.toHub()

        e.joinMessage(Component.translatable(
            "msg.myshore.hub.player_join",
            Component.text(player.name)
        ))
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.player.world.name != Hub.world.name) return
        if (e.player.gameMode == GameMode.CREATIVE) return
        e.isCancelled = true
    }

    @EventHandler
    fun onPlayerAttack(e: PrePlayerAttackEntityEvent) {
        if (e.player.world.name != Hub.world.name) return
        if (e.player.gameMode == GameMode.CREATIVE) return
        e.isCancelled = true
    }
}