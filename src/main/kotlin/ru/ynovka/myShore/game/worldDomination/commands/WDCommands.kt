package ru.ynovka.myShore.game.worldDomination.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentOnePlayer
import dev.jorel.commandapi.kotlindsl.playerExecutor
import org.bukkit.entity.Player
import ru.ynovka.myShore.game.worldDomination.WDGame.Companion.currentWDGame
import ru.ynovka.myShore.game.worldDomination.states.WDDistributionPlayers


object WDCommands {
    fun register() {
        val inviteViceSub = CommandAPICommand("invite_vice").apply {
            entitySelectorArgumentOnePlayer("vice")
            playerExecutor { president, args ->
                val vice = args["vice"] as Player
                val game = president.uniqueId.currentWDGame() ?: return@playerExecutor
                val state = game.fsm.current as? WDDistributionPlayers ?: return@playerExecutor

                state.inviteVice(vice, president)
            }
        }

        val acceptInviteViceSub = CommandAPICommand("accept_invite_vice").apply {
            entitySelectorArgumentOnePlayer("president")
            playerExecutor { vice, args ->
                val president = args["president"] as Player
                val game = vice.uniqueId.currentWDGame() ?: return@playerExecutor
                val state = game.fsm.current as? WDDistributionPlayers ?: return@playerExecutor

                state.acceptInviteVice(vice.uniqueId, president, game)
            }
        }

        val denyInviteViceSub = CommandAPICommand("deny_invite_vice").apply {
            entitySelectorArgumentOnePlayer("president")
            playerExecutor { vice, args ->
                val president = args["president"] as Player
                val game = vice.uniqueId.currentWDGame() ?: return@playerExecutor
                val state = game.fsm.current as? WDDistributionPlayers ?: return@playerExecutor

                state.denyInviteVice(vice, president)
            }
        }

        commandAPICommand("wd") {
            withSubcommand(inviteViceSub)
            withSubcommand(acceptInviteViceSub)
            withSubcommand(denyInviteViceSub)
        }
    }
}