package ru.ynovka.myShore.utils

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import ru.ynovka.myShore.MyShore.Companion.inst
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID


/**
 * - Task создаётся при первом permanent-сообщении
 * - 1 task = 1 игрок
 * - Task удаляется при clear()
 */
object ActionBarController {
    private const val INITIAL_DELAY = 1L
    private const val PERIOD_TICKS = 2L

    private val tasks = ConcurrentHashMap<UUID, ScheduledTask>()
    private val messages = ConcurrentHashMap<UUID, Component>()

    fun send(player: Player, message: Component) {
        val uuid = player.uniqueId
        messages[uuid] = message

        tasks.compute(uuid) { _, existing ->
            if (existing == null || existing.isCancelled) {
                startTask(player)
            } else {
                existing
            }
        }
    }

    fun clear(player: Player) {
        val uuid = player.uniqueId

        tasks.remove(uuid)?.cancel()
        messages.remove(uuid)

        player.scheduler.run(
            inst,
            { player.sendActionBar(Component.empty()) },
            null
        )
    }

    private fun startTask(player: Player): ScheduledTask {
        val uuid = player.uniqueId

        return player.scheduler.runAtFixedRate(
            inst,
            { task ->
                val msg = messages[uuid]

                if (!player.isOnline || msg == null) {
                    task.cancel()
                    tasks.remove(uuid)
                    messages.remove(uuid)
                    return@runAtFixedRate
                }

                player.sendActionBar(msg)
            },
            null,
            INITIAL_DELAY,
            PERIOD_TICKS
        )!!
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
