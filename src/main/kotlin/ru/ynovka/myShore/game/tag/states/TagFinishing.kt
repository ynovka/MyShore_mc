package ru.ynovka.myShore.game.tag.states

import ru.ynovka.myShore.game.tag.statistics.TagPlayerStatistics.saveStats
import ru.ynovka.myShore.game.tag.TagPlayerSetup.applyFinishingInventory
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.tag.TagPlayerRoles
import ru.ynovka.myShore.utils.Utils.clearTeams
import ru.ynovka.myShore.game.tag.hasVictims
import ru.ynovka.myShore.game.tag.hasHunter
import ru.ynovka.myShore.game.tag.TagPlayer
import ru.ynovka.myShore.game.tag.TagGame
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorld
import net.kyori.adventure.title.Title
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.utils.canMove
import java.time.Duration


// 5 сек после игры - определение победителей
class TagFinishing(game: TagGame) : GameState<TagPlayer, GameWorld, TagGame>(game) {

    override fun onEnterState() {
        // Жертвы живы или охотник отсутствует → победа жертв
        val winnerRole = if (game.hasVictims() || !game.hasHunter()) {
            TagPlayerRoles.VICTIM
        } else {
            TagPlayerRoles.HUNTER
        }

        saveStats(game, winnerRole)

        game.gamePlayers.forEach { tagPlayer ->
            tagPlayer.role = TagPlayerRoles.UNDEFINED

            val player = tagPlayer.player
            scheduler.schedule {
                player.clearActivePotionEffects()
                player.applyFinishingInventory()
                player.showTitle(buildWinnerTitle(winnerRole))
                player.canMove(true)
                player.clearTeams()
            }
                .entity(player)
                .once()
        }

        game.scheduler.schedule {
            val nextState = if (game.gamePlayers.size >= 2) {
                TagVoting(game)
            } else {
                TagWaitingForPlayers(game)
            }
            game.fsm.transitionTo(nextState)
        }
            .after(5 * 20L, Clock.TICKS)
            .once()

        game.map.onGameEnd(game)
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

    override fun canPlayerJoin(gamePlayer: TagPlayer): Boolean = false
}