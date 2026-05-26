package ru.ynovka.myShore

import ru.ynovka.myShore.game.gameUtils.CosmeticFireworkListener
import ru.ynovka.myShore.game.gameUtils.VisibilityGroupEvents
import ru.ynovka.myShore.game.pillars.PillarsEvents
import ru.ynovka.myShore.utils.MovementController
import ru.ynovka.myShore.text.ChatEvents
import ru.ynovka.myShore.hub.HubEvents


object Events {
    fun register() {

        HubEvents.register()
        ChatEvents.register()
        PillarsEvents.register()
        MovementController.register()
        VisibilityGroupEvents.register()
        CosmeticFireworkListener.register()
    }
}