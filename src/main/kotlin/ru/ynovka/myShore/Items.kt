package ru.ynovka.myShore

import ru.ynovka.myShore.games.tag.TagItems
import ru.ynovka.myShore.games.worldDomination.WDItems
import ru.ynovka.myShore.hub.HubItems


object Items {
    fun register() {
        TagItems.register()
        WDItems.register()
        HubItems.register()
    }
}