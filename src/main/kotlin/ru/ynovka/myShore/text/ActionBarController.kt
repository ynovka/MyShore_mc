package ru.ynovka.myShore.text

import ru.ynovka.myShore.MyShore.Companion.inst
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID


object ActionBarController {
    private val messages = ConcurrentHashMap<UUID, Component>()
    private var taskId = -1

    fun send(player: Player, message: Component) {
        messages[player.uniqueId] = message
        ensureTaskRunning()
    }

    fun clear(player: Player) {
        messages.remove(player.uniqueId)
        player.sendActionBar(Component.empty())
        if (messages.isEmpty()) stopTask()
    }

    private fun ensureTaskRunning() {
        if (taskId != -1) return
        taskId = inst.server.scheduler
            .runTaskTimer(inst, Runnable {
                messages.keys.retainAll(inst.server.onlinePlayers.map { it.uniqueId }.toSet())
                inst.server.onlinePlayers.forEach { p ->
                    messages[p.uniqueId]?.let { p.sendActionBar(it) }
                }
                if (messages.isEmpty()) stopTask()
            }, 1L, 2L).taskId
    }

    private fun stopTask() {
        inst.server.scheduler.cancelTask(taskId)
        taskId = -1
    }
}


/**
 * Постоянный ActionBar (обновляется автоматически)
 */
fun Player.sendPermanentActionBar(message: Component) {
    ActionBarController.send(this, message)
}

/**
 * Очистить ActionBar и остановить рендер
 */
fun Player.clearActionBar() {
    ActionBarController.clear(this)
}
