package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.statistics.TagPlayerStatistics.saveStats
import ru.ynovka.myShore.games.tag.TagPlayerSetup.applyFinishingInventory
import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.games.tag.TagGameStates
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.Utils.clearTeams
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.games.tag.hasVictims
import ru.ynovka.myShore.games.tag.hasHunter
import ru.ynovka.myShore.games.tag.TagGame
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.games.GameState
import net.kyori.adventure.title.Title
import ru.ynovka.myShore.utils.canMove
import org.bukkit.entity.Player
import java.time.Duration


// 5 сек после игры - определение победителей
object TagFinishingState : GameState<TagGame> {

    override fun onStateStart(game: TagGame) {
        // Жертвы живы или охотник отсутствует → победа жертв
        val winnerRole = if (game.hasVictims() || !game.hasHunter()) {
            TagPlayerRoles.VICTIM
        } else {
            TagPlayerRoles.HUNTER
        }

        saveStats(game, winnerRole)

        game.lobby.members.asPlayers().forEach { player ->
            player.clearActivePotionEffects()
            player.applyFinishingInventory()
            player.showTitle(buildWinnerTitle(winnerRole))
            player.canMove(true)
            player.clearTeams()
            game.players[player.uniqueId] = TagPlayerRoles.UNDEFINED
        }

        game.scheduler.runTaskLater(inst, Runnable {
            val nextState = if (game.players.size >= 2) {
                TagGameStates.VOTING
            } else {
                TagGameStates.WAITING_FOR_PLAYERS
            }
            game.transitionTo(nextState)
        }, 5 * 20L)

        game.map.onGameEnd(game)
    }

    override fun onPlayerJoin(game: TagGame, player: Player) {
        game.players[player.uniqueId] = TagPlayerRoles.SPECTATOR
        player.setupAsSpectator(game)
    }

    private fun buildWinnerTitle(winnerRole: TagPlayerRoles): Title {
        val comp = when (winnerRole) {
            TagPlayerRoles.VICTIM -> {
                Component.translatable("sub.title.myshore.tag.victory.runners")
            }

            TagPlayerRoles.HUNTER -> {
                Component.translatable("sub.title.myshore.tag.victory.hunter")
            }

            else -> Component.text("")
        }
        return Title.title(
            Component.text(""), comp,
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
        )
    }
}