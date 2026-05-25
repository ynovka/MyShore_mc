package ru.ynovka.myShore.hub

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.MyShore.Companion.scheduler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.entity.PlayerDeathEvent
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.ynovka.myShore.party.PartyManager
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.party.LeftReason
import ru.ynovka.myShore.hub.Hub.toHub
import org.bukkit.event.EventPriority
import org.bukkit.event.EventHandler
import ru.ynovka.myShore.MyShore
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.GameMode
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


object HubEvents : Listener {
    private val waterTicks = ConcurrentHashMap<UUID, Int>()

    private fun removeWaterTick(player: Player) {
        waterTicks.remove(player.uniqueId)

        scheduler.schedule {
            player.removePotionEffect(PotionEffectType.DARKNESS)
        }.entity(player).once()
    }

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)

        scheduler.schedule {
            val players = Bukkit.getOnlinePlayers()

            players.forEach { player ->
                scheduler.schedule {
                    val playerLoc = player.location

                    scheduler.schedule {
                        val blockLoc = playerLoc.clone()
                            .toHighestLocation()
                            .add(0.0, -12.0, 0.0)

                        val block = playerLoc.world.getBlockAt(blockLoc)

                        if (block.isLiquid && playerLoc.y <= 101.0) {
                            scheduler.schedule {
                                player.addPotionEffect(
                                    PotionEffect(
                                        PotionEffectType.DARKNESS,
                                        -1,
                                        0,
                                        false,
                                        false,
                                        false
                                    )
                                )

                                val waterSeconds = waterTicks.merge(
                                    player.uniqueId,
                                    1,
                                    Int::plus
                                ) ?: 1

                                if (waterSeconds >= 6) {
                                    player.playSound(
                                        player.location,
                                        Sound.ENTITY_ELDER_GUARDIAN_CURSE,
                                        1.0f,
                                        0.6f
                                    )

                                    scheduler.schedule {
                                        player.toHub()
                                        removeWaterTick(player)
                                        player.addPotionEffect(
                                            PotionEffect(
                                                PotionEffectType.BLINDNESS,
                                                40,
                                                0,
                                                false,
                                                false,
                                                false
                                            )
                                        )
                                    }.entity(player).after(10L, Clock.TICKS).once()
                                }
                            }.entity(player).once()
                        } else {
                            val currentTicks = waterTicks[player.uniqueId] ?: return@schedule

                            val newTicks = currentTicks - 1

                            if (newTicks > 0) {
                                waterTicks[player.uniqueId] = newTicks
                            } else {
                                removeWaterTick(player)
                            }
                        }
                    }.region(playerLoc).once()
                }.entity(player).once()
            }
        }
            .global()
            .repeatEvery(20L, Clock.TICKS)
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
        removeWaterTick(e.player)
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
        if (e.isCancelled) return
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