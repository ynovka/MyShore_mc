package ru.ynovka.myShore

import ru.ynovka.myShore.hub.HubCommands
import ru.ynovka.myShore.party.PartyCommands


object Commands {
    fun register() {
        PartyCommands.register()
        HubCommands.register()
    }
}