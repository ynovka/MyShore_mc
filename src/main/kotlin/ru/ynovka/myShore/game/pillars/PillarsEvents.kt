package ru.ynovka.myShore.game.pillars

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import com.github.darksoulq.abyssallib.server.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.GamePlayer.Companion.forEachOnlinePlayer
import ru.ynovka.myShore.game.SpectatorReason
import ru.ynovka.myShore.game.gameUtils.PlayerDeathMessages
import ru.ynovka.myShore.game.pillars.Pillar.Companion.TOP_BLOCK
import ru.ynovka.myShore.game.pillars.PillarsGame.Companion.currentPillarsGame
import ru.ynovka.myShore.game.pillars.states.PillarsInProgress

object PillarsEvents : Listener {

    private var fallCheckTask: ScheduledTask? = null
    private var entityCleanupTask: ScheduledTask? = null

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)

        startFallCheckTask()
        startEntityCleanupTask()
    }

    private fun startFallCheckTask() {
        if (fallCheckTask != null) return

        fallCheckTask = scheduler.schedule {
            inst.server.onlinePlayers
                .sortedBy { it.location.y }
                .forEach { player ->
                    checkPlayerFall(player)
                }
        }.global().repeatEvery(10L, Clock.TICKS)
    }

    private fun startEntityCleanupTask() {
        if (entityCleanupTask != null) return

        entityCleanupTask = scheduler.schedule {
            inst.server.worlds.forEach worldLoop@{ world ->
                if (!world.isPillarsWorld()) return@worldLoop

                world.entities.forEach entityLoop@{ entity ->
                    if (entity is Player) return@entityLoop
                    if (entity.location.y >= 0.0) return@entityLoop

                    scheduler.schedule {
                        entity.remove()
                    }.entity(entity).once()
                }
            }
        }.global().repeatEvery(100L, Clock.TICKS)
    }

    private fun checkPlayerFall(player: Player) {
        if (!player.world.isPillarsWorld()) return
        if (player.gameMode != GameMode.SURVIVAL) return

        val game = player.uniqueId.currentPillarsGame() ?: return
        val pPlayer = game.getOrCreatePlayer(player.uniqueId)
        pPlayer.updateLastKnownY(player.location.y)

        if (player.location.y > 0.0) return

        when (game.fsm.current) {
            is PillarsInProgress -> {
                if (!game.eliminatePlayer(player, pPlayer)) return

                pPlayer.withOnlinePlayer { onlinePlayer ->
                    onlinePlayer.gameMode = GameMode.SPECTATOR

                    game.gameWorld.get()?.let { world ->
                        onlinePlayer.teleportAsync(Location(world, 0.0, 110.0, 0.0))
                    }

                    game.broadcast(PlayerDeathMessages.voidFall(onlinePlayer))
                }
            }

            else -> {
                val pillar = game.gameWorld.pillars
                    .firstOrNull { it.owner == pPlayer.playerId }
                    ?: return

                val world = game.gameWorld.get() ?: return

                pPlayer.withOnlinePlayer { onlinePlayer ->
                    onlinePlayer.teleportAsync(
                        Location(
                            world,
                            pillar.x + 0.5,
                            TOP_BLOCK + 1.0,
                            pillar.z + 0.5
                        )
                    )
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = false)
    fun onPlayerDamage(e: EntityDamageEvent) {
        println("0 isCancelled == ${e.isCancelled}")
        val player = e.entity as? Player ?: return
        println("00")

        if (!player.world.isPillarsWorld()) return

        println("1")
        val game = player.uniqueId.currentPillarsGame() ?: return
        println("2")

        if (game.fsm.current is PillarsInProgress) return
        println("3")

        e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = false)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val player = e.player

        println("11 isCancelled == ${e.isCancelled}")
        if (!player.world.isPillarsWorld()) return

        println("22")
        val game = player.uniqueId.currentPillarsGame() ?: return
        val pPlayer = game.getOrCreatePlayer(player.uniqueId)

        val drops = e.drops
            .filter { !it.type.isAir && it.amount > 0 }
            .map { it.clone() }

        println("33")
        e.isCancelled = true

        e.drops.clear()
        e.droppedExp = 0

        when (game.fsm.current) {
            is PillarsInProgress -> {
                if (!game.eliminatePlayer(player, pPlayer)) return

                try {
                    dropDeathItems(player, drops)
                } catch (throwable: Throwable) {
                    println("555")
                }

                game.broadcast(PlayerDeathMessages.from(e))
            }
        }
    }

    private fun PillarsGame.eliminatePlayer(player: Player, pPlayer: PillarsPlayer): Boolean {
        val shouldHandleElimination = pPlayer.markEliminated() || pPlayer in activePlayers

        if (!shouldHandleElimination && pPlayer in spectatorPlayers) return false

        if (player.isInsideVehicle) {
            player.leaveVehicle()
        }

        return movePlayerToSpectator(pPlayer, SpectatorReason.ELIMINATED) && shouldHandleElimination
    }

    private fun dropDeathItems(player: Player, drops: List<ItemStack>) {
        val dropLocation = player.location.clone()
            .add(0.0, player.eyeHeight - 0.3, 0.0)

        for (stack in drops) {
            if (stack.type.isAir || stack.amount <= 0) continue

            scheduler.schedule {
                dropLocation.world.dropItem(dropLocation, stack.clone()) { item ->
                    item.pickupDelay = 10
                    item.velocity = randomDeathDropVelocity()
                }
            }.region(dropLocation).once()
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

    @EventHandler
    fun onPlayerOpenEnderChest(e: InventoryOpenEvent) {
        if (!e.player.world.isPillarsWorld()) return
        if (e.inventory.type != InventoryType.ENDER_CHEST) return

        e.isCancelled = true
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
