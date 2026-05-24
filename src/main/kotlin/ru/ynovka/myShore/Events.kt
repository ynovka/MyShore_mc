package ru.ynovka.myShore

import ru.ynovka.myShore.game.gameUtils.VisibilityGroupEvents
import ru.ynovka.myShore.game.worldDomination.WDEvents
import ru.ynovka.myShore.game.pillars.PillarsEvents
import ru.ynovka.myShore.utils.MovementController
import ru.ynovka.myShore.text.chat.ChatEvents
import ru.ynovka.myShore.game.tag.TagEvents
import ru.ynovka.myShore.hub.HubEvents


object Events {
    fun register() {

        WDEvents.register()
        HubEvents.register()
        TagEvents.register()
        ChatEvents.register()
        PillarsEvents.register()
        MovementController.register()
        VisibilityGroupEvents.register()
    }
}