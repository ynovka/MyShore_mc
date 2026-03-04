package ru.ynovka.myShore.games.tag.states

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.PlayerRoles
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagGameStates
import ru.ynovka.myShore.games.tag.TagPlayerSetup.applyFinishingInventory
import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.games.tag.hasHunter
import ru.ynovka.myShore.games.tag.hasVictims
import ru.ynovka.myShore.utils.Utils.asPlayer
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.utils.Utils.clearTeams
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.utils.sendPermanentActionBar


// 5 сек после игры - определение победителей
object FinishingState : TagState {

    override fun onStateStart(game: TagGame) {
        // Жертвы живы или охотник отсутствует → победа жертв
        val winnerRole = if (game.hasVictims() || !game.hasHunter()) {
            PlayerRoles.VICTIM
        } else {
            PlayerRoles.HUNTER
        }

        // todo
        // saveStats(game, winnerRole)

        val actionBarMsg = buildWinnerActionBar(winnerRole)
        val chatMsg = buildWinnerChatMsg(game, winnerRole)

        game.lobby.members.asPlayers().forEach { player ->
            player.clearActivePotionEffects()
            player.applyFinishingInventory()
            player.sendPermanentActionBar(actionBarMsg)
            player.sendMessage(chatMsg)
            player.canMove(true)
            player.clearTeams()
            game.players[player.uniqueId] = PlayerRoles.UNDEFINED
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
        game.players[player.uniqueId] = PlayerRoles.SPECTATOR
        player.setupAsSpectator(game)
    }

    // ---------- приватные хелперы ----------

    private fun buildWinnerActionBar(winnerRole: PlayerRoles): Component {
        // todo перевод
        val text = if (winnerRole == PlayerRoles.VICTIM) "раннеры победили!" else "охотник победил!"
        return Component.text(text)
    }

    private fun buildWinnerChatMsg(game: TagGame, winnerRole: PlayerRoles): Component =
        when (winnerRole) {
            PlayerRoles.VICTIM -> {
                val runnerNames = game.players
                    .filterValues { it == PlayerRoles.VICTIM || it == PlayerRoles.SPECTATOR_VICTIM }
                    .keys.asPlayers()
                    .joinToString(", ") { it.name }

                Component.translatable(
                    "msg.myshore.tag.victory.runners",
                    Component.text(runnerNames)
                )
            }

            PlayerRoles.HUNTER -> {
                val hunterName = game.players
                    .filterValues { it == PlayerRoles.HUNTER }
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