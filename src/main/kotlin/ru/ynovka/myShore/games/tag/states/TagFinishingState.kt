package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.statistics.TagPlayerStatistics.saveStats
import ru.ynovka.myShore.games.tag.TagPlayerSetup.applyFinishingInventory
import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.Utils.clearTeams
import ru.ynovka.myShore.games.tag.hasVictims
import ru.ynovka.myShore.games.tag.hasHunter
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagPlayer
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import ru.ynovka.myShore.utils.canMove
import java.time.Duration


// 5 сек после игры - определение победителей
object TagFinishingState : GameState<TagPlayer> {

    override fun onEnter(game: Game<TagPlayer>) {
        val tagGame = game as TagGame

        // Жертвы живы или охотник отсутствует → победа жертв
        val winnerRole = if (tagGame.hasVictims() || !tagGame.hasHunter()) {
            TagPlayerRoles.VICTIM
        } else {
            TagPlayerRoles.HUNTER
        }

        saveStats(tagGame, winnerRole)

        tagGame.players.forEach { tagPlayer ->
            val player = tagPlayer.player
            player.clearActivePotionEffects()
            player.applyFinishingInventory()
            player.showTitle(buildWinnerTitle(winnerRole))
            player.canMove(true)
            player.clearTeams()
            tagPlayer.role = TagPlayerRoles.UNDEFINED
        }

        tagGame.scheduler.runTaskLater(inst, Runnable {
            val nextState = if (tagGame.players.size >= 2) {
                TagVotingState
            } else {
                TagWaitingForPlayersState
            }
            tagGame.fsm.transitionTo(nextState)
        }, 5 * 20L)

        tagGame.map.onGameEnd(tagGame)
    }

    override fun onPlayerJoin(game: Game<TagPlayer>, player: TagPlayer) {
        val tagGame = game as TagGame
        player.role = TagPlayerRoles.SPECTATOR
        player.player.setupAsSpectator(tagGame)
    }

    private fun buildWinnerTitle(winnerRole: TagPlayerRoles): Title {
        val comp = when (winnerRole) {
            TagPlayerRoles.VICTIM -> Component.translatable("sub.title.myshore.tag.victory.runners")
            TagPlayerRoles.HUNTER -> Component.translatable("sub.title.myshore.tag.victory.hunter")
            else -> Component.text("")
        }
        return Title.title(
            Component.text(""), comp,
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
        )
    }
}