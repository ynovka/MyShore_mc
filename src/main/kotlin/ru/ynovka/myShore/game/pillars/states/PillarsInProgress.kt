package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.pillars.Pillar.Companion.TELEPORT_Y
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.SpectatorReason
import org.bukkit.potion.PotionEffectType
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.utils.canMove
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.Material
import org.bukkit.GameMode
import org.bukkit.Location
import ru.ynovka.myShore.utils.restrictToBlock


class PillarsInProgress(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        // удаляем колбы
        val world = game.gameWorld.getOrCreate().get()
        game.gameWorld.pillars.forEach { pillar ->
            val blockLoc = Location(world, pillar.x.toDouble(), TELEPORT_Y - 1, pillar.z.toDouble())

            scheduler.schedule {
                val centerX = blockLoc.blockX
                val centerZ = blockLoc.blockZ

                for (x in centerX - 2..centerX + 2) {
                    for (z in centerZ - 2..centerZ + 2) {
                        for (y in 105..125) {
                            world.getBlockAt(x, y, z).type = Material.AIR
                        }
                    }
                }
            }.region(blockLoc).once()
        }

        // даём эффект плавного падения
        game.gamePlayers.asPlayers().forEach { player ->
            scheduler.schedule {
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.SLOW_FALLING,
                        50,
                        0,
                        false,
                        false,
                        false
                    )
                )
            }.entity(player).once()
        }

        // даём возможность двигаться по приземлению
        scheduler.schedule {
            game.gamePlayers.asPlayers().forEach { player ->
                scheduler.schedule {
                    player.restrictToBlock(false)
                    player.gameMode = GameMode.SURVIVAL
                }.entity(player).once()
            }
        }.after(60L, Clock.TICKS).once()

        // Выдача рандом предметов
        startGiveRandomItemsTimer()

        // Барьер
        // todo ^
    }

    override fun onPlayerBecomeSpectator(gamePlayer: PillarsPlayer, reason: SpectatorReason) {
        if (game.gamePlayers.size != 1) return

        // todo Победитель !!
        val winner = game.gamePlayers.firstOrNull() ?: return
        game.fsm.transitionTo(PillarsFinishing(game))
    }

    override fun onPlayerLeave(gamePlayer: PillarsPlayer) {
        if (game.gamePlayers.size != 1) return

        // todo Победитель !!
        val winner = game.gamePlayers.firstOrNull() ?: return
        game.fsm.transitionTo(PillarsFinishing(game))
    }

    private fun startGiveRandomItemsTimer() {
        scheduler.schedule {
            game.gamePlayers.asPlayers().forEach { player ->
                scheduler.schedule {
                    player.inventory.addItem(ItemStack.of(items.random()))
                }.entity(player).once()
            }
        }
            .repeatWhile { game.fsm.current is PillarsInProgress }
            .repeatEvery(7 * 20L, Clock.TICKS)
    }

    override fun canPlayerJoin(gamePlayer: PillarsPlayer) = false

    companion object {
        @Suppress("removal")
        val excludedItems = setOf(
            Material.AIR,
            Material.COMMAND_BLOCK,
            Material.COMMAND_BLOCK_MINECART,
            Material.CHAIN_COMMAND_BLOCK,
            Material.LEGACY_COMMAND,
            Material.LEGACY_COMMAND_CHAIN,
            Material.REPEATING_COMMAND_BLOCK,
            Material.LEGACY_COMMAND_MINECART,
            Material.LEGACY_COMMAND_REPEATING,
            Material.NETHER_PORTAL,
            Material.LEGACY_PORTAL,
            Material.END_PORTAL,
            Material.LEGACY_ENDER_PORTAL,
            Material.LIGHT
        )

        val items = Material.entries.toMutableSet().apply {
            removeAll(excludedItems)
        }
    }
}