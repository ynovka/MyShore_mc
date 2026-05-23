package ru.ynovka.myShore

import ru.ynovka.myShore.game.tag.TagItems
import ru.ynovka.myShore.game.worldDomination.WDItems
import ru.ynovka.myShore.hub.HubItems


object Items {
    fun register() {
        TagItems.register()
        WDItems.register()
        HubItems.register()
    }
}