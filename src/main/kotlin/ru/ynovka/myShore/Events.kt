package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.server.event.EventBus
import ru.ynovka.myShore.antiCheat.AntiCheatEvents
import ru.ynovka.myShore.utils.MovementController
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.TagEvents
import ru.ynovka.myShore.hub.HubEvents


object Events {
    fun register() {
        HubEvents.register()
        TagEvents.register()
        MovementController.register()

        val bus = EventBus(inst)
        bus.register(AntiCheatEvents)
    }
}