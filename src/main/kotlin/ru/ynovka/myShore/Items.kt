package ru.ynovka.myShore

import ru.ynovka.myShore.game.pillars.PillarsItems
import ru.ynovka.myShore.hub.HubItems


object Items {
    fun register() {
        HubItems.register()
        PillarsItems.register()
    }
}
