package ru.ynovka.myShore.games.tag.states

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupForVoting
import ru.ynovka.myShore.games.tag.maps.TagGameMap
import ru.ynovka.myShore.games.tag.TagGameStates
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.GameState
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.Sound
import kotlin.math.ceil


// 10 сек на голосование за карту, режим игры, сменить лобби, посмотреть статистику
object VotingState : GameState {

    override fun onStateStart(game: TagGame) {
        game.lobby.members.asPlayers().forEach { player ->
            player.setupForVoting(game)
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f)
        }

        game.scheduler.runTaskLater(inst, Runnable {
            if (game.state != TagGameStates.VOTING) return@Runnable

            resolveMapVoting(game)?.let { selectedMap ->
                game.map = selectedMap
                game.lobby.members.asPlayers().forEach { player ->
                    val mapNameComp = ServerTranslator.translate(
                        selectedMap.mapName, player
                    )
                    player.sendMessage(
                        Component.translatable("msg.myshore.tag.choosen_map", mapNameComp)
                    )
                    // todo вывести авторов карты и характеристику
                }
            }
            game.mapVotes.clear()
            game.transitionTo(TagGameStates.PREPARING)
        }, 10 * 20L)
    }

    override fun onPlayerJoin(game: TagGame, player: Player) {
        player.setupForVoting(game)
    }

    /**
     * Определяет карту по итогам голосования.
     * Возвращает null, если голосов меньше 1/3 от числа игроков.
     * При равенстве — случайная из лидеров.
     */
    private fun resolveMapVoting(game: TagGame): TagGameMap? {
        val votes = game.mapVotes.values
        if (votes.isEmpty()) return null

        val threshold = ceil(game.lobby.members.size / 3.0).toInt()
        if (votes.size < threshold) return null

        val grouped = votes.groupingBy { it }.eachCount()
        val maxVotes = grouped.values.max()
        val winners = grouped.filterValues { it == maxVotes }.keys.toList()

        return if (winners.size == 1) winners.first() else winners.random()
    }
}