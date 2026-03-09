package ru.ynovka.myShore.games.tag.states

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagGameStates
import ru.ynovka.myShore.games.tag.TagPlayerSetup.applyFinishingInventory
import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.games.tag.TagStats.saveStats
import ru.ynovka.myShore.games.tag.hasHunter
import ru.ynovka.myShore.games.tag.hasVictims
import ru.ynovka.myShore.utils.Utils.asPlayer
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.utils.Utils.clearTeams
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.utils.sendPermanentActionBar
import ru.ynovka.myShore.games.GameState


// 5 сек после игры - определение победителей
object FinishingState : GameState {

    override fun onStateStart(game: TagGame) {
        // Жертвы живы или охотник отсутствует → победа жертв
        val winnerRole = if (game.hasVictims() || !game.hasHunter()) {
            TagPlayerRoles.VICTIM
        } else {
            TagPlayerRoles.HUNTER
        }

        saveStats(game, winnerRole)

        val actionBarMsg = buildWinnerActionBar(winnerRole)
        val chatMsg = buildWinnerChatMsg(game, winnerRole)

        game.lobby.members.asPlayers().forEach { player ->
            player.clearActivePotionEffects()
            player.applyFinishingInventory()
            player.sendPermanentActionBar(actionBarMsg)
            player.sendMessage(chatMsg)
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
    }

    override fun onPlayerJoin(game: TagGame, player: Player) {
        game.players[player.uniqueId] = TagPlayerRoles.SPECTATOR
        player.setupAsSpectator(game)
    }

    // ---------- приватные хелперы ----------

    private fun buildWinnerActionBar(winnerRole: TagPlayerRoles): Component {
        return if (winnerRole == TagPlayerRoles.VICTIM)
            Component.translatable("bar.myshore.tag.victory.runners")
        else
            Component.translatable("bar.myshore.tag.victory.hunter")
    }

    private fun buildWinnerChatMsg(game: TagGame, winnerRole: TagPlayerRoles): Component =
        when (winnerRole) {
            TagPlayerRoles.VICTIM -> {
                val runnerNames = game.players
                    .filterValues { it == TagPlayerRoles.VICTIM || it == TagPlayerRoles.SPECTATOR_VICTIM }
                    .keys.asPlayers()
                    .joinToString(", ") { it.name }

                Component.translatable(
                    "msg.myshore.tag.victory.runners",
                    Component.text(runnerNames)
                )
            }

            TagPlayerRoles.HUNTER -> {
                val hunterName = game.players
                    .filterValues { it == TagPlayerRoles.HUNTER }
                    .keys.firstOrNull()
                    ?.asPlayer()?.name
                    ?: "?"

                Component.translatable(
                    "msg.myshore.tag.victory.hunter",
                    Component.text(hunterName)
                )
            }

            else -> Component.text("")
        }
}