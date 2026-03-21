package ru.ynovka.myShore.games.pillars

import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.Listener


object PillarsEvents : Listener{
    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }
}