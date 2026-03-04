package ru.ynovka.myShore

import ru.ynovka.myShore.hub.HubEvents
import ru.ynovka.myShore.utils.MovementController

object Events {
    fun register() {
        HubEvents.regsiter()
        MovementController.register()
    }
}