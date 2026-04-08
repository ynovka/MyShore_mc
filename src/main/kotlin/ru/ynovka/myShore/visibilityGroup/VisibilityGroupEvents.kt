package ru.ynovka.myShore.visibilityGroup

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import ru.ynovka.myShore.MyShore.Companion.inst

object VisibilityGroupEvents : Listener {

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        VisibilityGroup.onPlayerQuit(e.player.uniqueId)
    }

}