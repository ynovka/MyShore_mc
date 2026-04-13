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
import ru.ynovka.myShore.games.GameState
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import ru.ynovka.myShore.utils.canMove
import java.time.Duration


// 5 сек после игры - определение победителей
class TagFinishing(game: TagGame) : GameState<TagPlayer, TagGame>(game) {

    override fun onEnter() {
        // Жертвы живы или охотник отсутствует → победа жертв
        val winnerRole = if (game.hasVictims() || !game.hasHunter()) {
            TagPlayerRoles.VICTIM
        } else {
            TagPlayerRoles.HUNTER
        }

        saveStats(game, winnerRole)

        game.gamePlayers.forEach { tagPlayer ->
            val player = tagPlayer.player
            player.clearActivePotionEffects()
            player.applyFinishingInventory()
            player.showTitle(buildWinnerTitle(winnerRole))
            player.canMove(true)
            player.clearTeams()
            tagPlayer.role = TagPlayerRoles.UNDEFINED
        }

        game.scheduler.runTaskLater(inst, Runnable {
            val nextState = if (game.gamePlayers.size >= 2) {
                TagVoting(game)
            } else {
                TagWaitingForPlayers(game)
            }
            game.fsm.transitionTo(nextState)
        }, 5 * 20L)

        game.map.onGameEnd(game)
    }

    override fun onPlayerJoin(gamePlayer: TagPlayer) {
        gamePlayer.role = TagPlayerRoles.SPECTATOR
        gamePlayer.player.setupAsSpectator(game)
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