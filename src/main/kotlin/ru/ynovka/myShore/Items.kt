package ru.ynovka.myShore

import ru.ynovka.myShore.games.tag.TagItems
import ru.ynovka.myShore.hub.HubItems


object Items {
    fun register() {
        TagItems.register()
        HubItems.register()
    }
}