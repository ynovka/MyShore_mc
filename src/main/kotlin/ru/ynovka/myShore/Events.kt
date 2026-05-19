package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.server.event.EventBus
import ru.ynovka.myShore.antiCheat.AntiCheatEvents
import ru.ynovka.myShore.utils.MovementController
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.game.tag.TagEvents
import ru.ynovka.myShore.game.worldDomination.WDEvents
import ru.ynovka.myShore.hub.HubEvents
import ru.ynovka.myShore.text.chat.ChatEvents
import ru.ynovka.myShore.visibilityGroup.VisibilityGroupEvents


object Events {
    fun register() {

        WDEvents.register()
        HubEvents.register()
        TagEvents.register()
        ChatEvents.register()
        MovementController.register()
        VisibilityGroupEvents.register()

        val bus = EventBus(inst)
        bus.register(AntiCheatEvents)
    }
}