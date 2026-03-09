package ru.ynovka.myShore.games.pillars

import org.bukkit.event.Listener
import ru.ynovka.myShore.MyShore.Companion.inst

object PillarsEvents : Listener{

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    
}