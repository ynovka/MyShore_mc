package ru.ynovka.myShore.event

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import net.kyori.adventure.text.format.NamedTextColor
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.CommandAPICommand
import net.kyori.adventure.text.Component


object EventCommands {
    fun register() {
        val createSub = CommandAPICommand("create").apply {
            playerExecutor { player, _ -> EventManager.create(player) }
        }

        val startSub = CommandAPICommand("start").apply {
            val gameArg = StringArgument("game")
            gameArg.replaceSuggestions(
                ArgumentSuggestions.strings { _ ->
                    EventManager.suggestGames()
                }
            )
            withArguments(gameArg)

            playerExecutor { player, args ->
                EventManager.start(player, args["game"] as String)
            }
        }

        val joinSub = CommandAPICommand("join").apply {
            playerExecutor { player, _ -> EventManager.join(player) }
        }

        val leaveSub = CommandAPICommand("leave").apply {
            playerExecutor { player, _ -> EventManager.leave(player) }
        }

        val finishSub = CommandAPICommand("finish").apply {
            playerExecutor { player, _ -> EventManager.finish(player) }
        }

        commandAPICommand("event") {
            playerExecutor { player, _ ->
                val event = EventManager.activeEvent
                if (event == null) {
                    player.sendMessage(
                        Component.translatable("msg.myshore.event.status.none")
                            .color(NamedTextColor.YELLOW)
                    )
                    return@playerExecutor
                }

                val stateKey = when (event.state) {
                    EventState.GATHERING -> "msg.myshore.event.state.gathering"
                    EventState.STARTED -> "msg.myshore.event.state.started"
                    EventState.FINISHED -> "msg.myshore.event.state.finished"
                }

                player.sendMessage(
                    Component.translatable(
                        "msg.myshore.event.status.active",
                        event.displayName,
                        Component.translatable(stateKey),
                        Component.text(event.party.members.size)
                    ).color(NamedTextColor.GOLD)
                )
            }

            withSubcommand(createSub)
            withSubcommand(startSub)
            withSubcommand(joinSub)
            withSubcommand(leaveSub)
            withSubcommand(finishSub)
        }
    }
}
