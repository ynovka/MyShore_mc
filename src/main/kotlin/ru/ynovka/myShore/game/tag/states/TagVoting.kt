package ru.ynovka.myShore.game.tag.states

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import ru.ynovka.myShore.game.tag.TagPlayerSetup.setupForWaitingOrVoting
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.game.gameUtils.ActionbarWaitingFor
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.tag.maps.TagMap
import ru.ynovka.myShore.game.tag.TagPlayer
import ru.ynovka.myShore.game.tag.teleport
import ru.ynovka.myShore.game.tag.TagGame
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorldOld
import org.bukkit.GameMode
import org.bukkit.Sound
import kotlin.math.ceil


// 10 сек на голосование за карту, режим игры, сменить лобби, посмотреть статистику
class TagVoting(game: TagGame) : GameState<TagPlayer, GameWorldOld, TagGame>(game) {

    override fun onEnterState() {
        ActionbarWaitingFor.startRendering(
            game = game,
            state = this,
            componentKey = "bar.myshore.tag.voting"
        )

        game.gamePlayers.asPlayers().forEach { player ->
            scheduler.schedule {
                player.setupForWaitingOrVoting(game)
                player.playSound(player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)
            }.entity(player).once()
        }

        game.scheduler.schedule {
            if (game.fsm.current !== this) return@schedule

            resolveMapVoting(game)?.let { setupMap(game, it) }
            game.mapVotes.clear()
            game.fsm.transitionTo(TagCountdown(this@TagVoting.game))
        }
            .after(10 * 20L, Clock.TICKS)
            .once()
    }

    override fun onPlayerJoin(gamePlayer: TagPlayer) {
        val player = gamePlayer.player
        scheduler.schedule {
            player.setupForWaitingOrVoting(game)
        }.entity(player).once()
    }

    /**
     * Определяет карту по итогам голосования.
     * Возвращает null, если голосов меньше 1/3 от числа игроков.
     * При равенстве — случайная из лидеров.
     */
    private fun resolveMapVoting(game: TagGame): TagMap? {
        val votes = game.mapVotes.values
        if (votes.isEmpty()) return null

        val threshold = ceil(game.gamePlayers.size / 3.0).toInt()
        if (votes.size < threshold) return null

        val grouped = votes.groupingBy { it }.eachCount()
        val maxVotes = grouped.values.max()
        val winners = grouped.filterValues { it == maxVotes }.keys.toList()

        return if (winners.size == 1) winners.first() else winners.random()
    }

    companion object {
        fun setupMap(game: TagGame, map: TagMap, shouldTeleport: Boolean = false) {
            game.map = map
            game.gamePlayers.asPlayers().forEach { player ->
                scheduler.schedule {
                    val mapNameComp = ServerTranslator.translate(map.mapName, player)
                    player.sendMessage(
                        Component.translatable("msg.myshore.tag.choosen_map", mapNameComp)
                    )

                    if (shouldTeleport) {
                        game.map.teleport(player, game) {
                            player.gameMode = GameMode.ADVENTURE
                        }
                    }
                }.entity(player).once()
            }
        }
    }
}