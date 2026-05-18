package ru.ynovka.myShore.games.pillars.states

import com.github.darksoulq.abyssallib.server.scheduler.TimeUnit
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import org.bukkit.GameMode
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.games.pillars.PillarsPlayer
import ru.ynovka.myShore.games.pillars.PillarsWorld
import ru.ynovka.myShore.games.pillars.PillarsGame
import ru.ynovka.myShore.games.GameState
import org.bukkit.inventory.ItemStack
import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import ru.ynovka.myShore.games.GamePlayer
import ru.ynovka.myShore.utils.canMove


class PillarsInProgress(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        // удаляем колбы
        // todo ^

        // даём эффект плавного падения
        game.gamePlayers.map(GamePlayer::player).forEach {
            it.addPotionEffect(
                PotionEffect(
                    PotionEffectType.SLOW_FALLING,
                    60,
                    0,
                    false,
                    false,
                    false
                )
            )
        }

        // даём возможность двигаться по приземлению
        scheduler.schedule {
            game.gamePlayers.map(GamePlayer::player).forEach {
                it.canMove(true)
                it.gameMode = GameMode.SURVIVAL
            }
        }.sync().after(60L, Clock.TICKS).once()

        // Выдача рандом предметов
        startGiveRandomItemsTimer()

        // Барьер
        // todo ^
    }

    private fun startGiveRandomItemsTimer() {
        scheduler.schedule {
            scheduler.schedule {
                game.gamePlayers.forEach { pPlayer ->
                    pPlayer.player.inventory.addItem(ItemStack.of(items.random()))
                }
            }.sync().once()
        }
            .async()
            .repeatWhile { game.fsm.current is PillarsInProgress }
            .repeatEvery(7, TimeUnit.SECONDS, Clock.REALTIME)
    }

    // todo listeners: onMove -> y<=0 - player to spec ; onDeath -> player to spec

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