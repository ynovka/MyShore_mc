package ru.ynovka.myShore.hub

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.worldDomination.WDGame
import ru.ynovka.myShore.hub.Hub.toHub


object HubCommands {
    fun register() {
        commandAPICommand("hub") {
            playerExecutor { player, _ ->
                player.toHub()
            }
        }
        commandAPICommand("testjoinpillars") {
            playerExecutor { player, _ ->
                GameManager.join(player, ::PillarsGame)
            }
        }
    }
}