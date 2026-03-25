package ru.ynovka.myShore.games.tag.states

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupForVoting
import ru.ynovka.myShore.games.tag.maps.TagMap
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagPlayer
import ru.ynovka.myShore.games.tag.teleport
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Sound
import kotlin.math.ceil


// 10 сек на голосование за карту, режим игры, сменить лобби, посмотреть статистику
object TagVotingState : GameState<TagPlayer> {

    override fun onEnter(game: Game<TagPlayer>) {
        val tagGame = game as TagGame
        tagGame.gamePlayers.forEach { tagPlayer ->
            tagPlayer.player.setupForVoting(tagGame)
            tagPlayer.player.playSound(tagPlayer.player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)
        }

        tagGame.scheduler.runTaskLater(inst, Runnable {
            if (tagGame.fsm.current != TagVotingState) return@Runnable

            resolveMapVoting(tagGame)?.let { setupMap(tagGame, it) }
            tagGame.mapVotes.clear()
            tagGame.fsm.transitionTo(TagPreparingState)
        }, 10 * 20L)
    }

    override fun onPlayerJoin(game: Game<TagPlayer>, player: TagPlayer) {
        player.player.setupForVoting(game as TagGame)
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

    fun setupMap(game: TagGame, map: TagMap, shouldTeleport: Boolean = false) {
        game.map = map
        game.gamePlayers.forEach { tagPlayer ->
            val mapNameComp = ServerTranslator.translate(map.mapName, tagPlayer.player)
            tagPlayer.player.sendMessage(
                Component.translatable("msg.myshore.tag.choosen_map", mapNameComp)
            )

            if (shouldTeleport) {
                game.map.teleport(tagPlayer.player, game) {
                    tagPlayer.player.gameMode = GameMode.ADVENTURE
                }
            }
        }
    }
}