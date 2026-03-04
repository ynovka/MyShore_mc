package ru.ynovka.myShore.utils

import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import ru.ynovka.myShore.MyShore.Companion.inst
import java.util.UUID


object MovementController : Listener {
    val canMove: MutableMap<UUID, Boolean> = mutableMapOf()

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player

        if (canMove[player.uniqueId] != false) return

        val from = event.from
        val to = event.to

        if (
            from.x != to.x ||
            from.y != to.y ||
            from.z != to.z
        ) {
            event.to = from.clone().apply {
                yaw = to.yaw
                pitch = to.pitch
            }
        }
    }
}

fun Player.canMove(value: Boolean) {
    MovementController.canMove[uniqueId] = value
    walkSpeed = if (value) 0.2f else 0f
}