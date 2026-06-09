package ru.ynovka.myShore.party

import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentOnePlayer
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.CommandAPICommand
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.text.translate
import org.bukkit.entity.Player


object PartyCommands {
    fun register() {

        val inviteSub = CommandAPICommand("invite").apply {
            entitySelectorArgumentOnePlayer("invitedPlayer")
            playerExecutor { player, args -> PartyManager.invite(player, args["invitedPlayer"] as Player) }
        }

        val acceptSub = CommandAPICommand("accept").apply {
            entitySelectorArgumentOnePlayer("partyOwner")
            playerExecutor { player, args -> PartyManager.acceptInvite(player, args["partyOwner"] as Player) }
        }

        val leaveSub = CommandAPICommand("leave").apply {
            playerExecutor { player, _ -> PartyManager.leave(player) }
        }

        val deleteSub = CommandAPICommand("delete").apply {
            playerExecutor { player, _ -> PartyManager.disband(player) }
        }

        val membersSub = CommandAPICommand("members").apply {
            withAliases("list")
            playerExecutor { player, _ -> PartyManager.showMembers(player) }
        }

        val kickSub = CommandAPICommand("kick").apply {
            entitySelectorArgumentOnePlayer("kickedPlayer") {
                replaceSuggestions(
                    ArgumentSuggestions.strings { info ->
                        val sender = info.sender as? Player ?: return@strings emptyArray()
                        PartyManager.suggestKickTargets(sender)
                    }
                )
            }
            playerExecutor { player, args -> PartyManager.kick(player, args["kickedPlayer"] as Player) }
        }

        val setOwnerSub = CommandAPICommand("setOwner").apply {
            entitySelectorArgumentOnePlayer("partyMember") {
                replaceSuggestions(
                    ArgumentSuggestions.strings { info ->
                        val sender = info.sender as? Player ?: return@strings emptyArray()
                        PartyManager.suggestOwnerTargets(sender)
                    }
                )
            }
            playerExecutor { player, args -> PartyManager.setOwner(player, args["partyMember"] as Player) }
        }

        commandAPICommand("p") {
            withAliases("party")
            playerExecutor { player, _ ->
                player.sendMessage(Component.translatable("msg.myshore.party.help").translate(player))
            }
            withSubcommand(inviteSub)
            withSubcommand(acceptSub)
            withSubcommand(leaveSub)
            withSubcommand(deleteSub)
            withSubcommand(membersSub)
            withSubcommand(kickSub)
            withSubcommand(setOwnerSub)
        }
    }
}
