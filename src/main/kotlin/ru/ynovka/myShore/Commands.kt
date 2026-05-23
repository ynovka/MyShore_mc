package ru.ynovka.myShore

import ru.ynovka.myShore.game.tag.TagCommands
import ru.ynovka.myShore.game.worldDomination.commands.WDCommands
import ru.ynovka.myShore.party.PartyCommands
import ru.ynovka.myShore.hub.HubCommands


object Commands {
    fun register() {
        PartyCommands.register()
        HubCommands.register()
        TagCommands.register()
        WDCommands.register()
    }
}