package ru.ynovka.myShore.text.actionBar

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import com.github.darksoulq.abyssallib.server.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.MyShore.Companion.scheduler
import java.util.UUID

object ActionBar {
    private val playerMessages = HashMap<UUID, HashMap<Int, ArrayDeque<ActionBarEntry>>>()
    private var task: ScheduledTask? = null

    fun send(player: Player, message: Component, priority: Int = 1, durationMs: Long? = null) {
        val entry = ActionBarEntry(
            message = message,
            priority = priority,
            expiresAt = durationMs?.let { System.currentTimeMillis() + it }
        )
        val deque = playerMessages
            .getOrPut(player.uniqueId) { HashMap() }
            .getOrPut(priority) { ArrayDeque() }

        merge(deque, entry)
        ensureTaskRunning()
    }

    fun clear(player: Player) {
        playerMessages.remove(player.uniqueId)
        player.sendActionBar(Component.empty())
    }

    private fun merge(deque: ArrayDeque<ActionBarEntry>, entry: ActionBarEntry) {
        if (deque.isEmpty()) {
            deque.addFirst(entry)
            return
        }
        val current = deque.first()
        when {
            current.isPermanent && entry.isPermanent -> {
                deque.clear()
                deque.addFirst(entry)
            }
            current.isPermanent && !entry.isPermanent -> {
                deque.addFirst(entry)
            }
            !current.isPermanent && entry.isPermanent -> {
                if (deque.size > 1) deque.removeLast()
                deque.addLast(entry)
            }
            else -> {
                val now = System.currentTimeMillis()
                val currentRemaining = current.expiresAt!! - now
                val newRemaining = entry.expiresAt!! - now

                if (newRemaining >= currentRemaining) {
                    deque.removeFirst()
                    deque.addFirst(entry)
                } else {
                    deque.addFirst(entry)
                }
            }
        }
    }

    private fun tick() {
        val online = inst.server.onlinePlayers.associateBy { it.uniqueId }
        playerMessages.keys.retainAll(online.keys)

        playerMessages.forEach { (uuid, byPriority) ->
            byPriority.values.forEach { deque ->
                while (deque.firstOrNull()?.isExpired() == true) deque.removeFirst()
            }
            byPriority.values.removeIf { it.isEmpty() }

            val topMessage = byPriority.maxByOrNull { it.key }?.value?.firstOrNull()?.message
            online[uuid]?.sendActionBar(topMessage ?: Component.empty())
        }

        playerMessages.values.removeIf { it.isEmpty() }
        if (playerMessages.isEmpty()) stopTask()
    }

    private fun ensureTaskRunning() {
        if (task != null) return
        task = scheduler.schedule { tick() }
            .sync()
            .after(1L, Clock.TICKS)
            .repeatEvery(2L, Clock.TICKS)
    }

    private fun stopTask() {
        task?.cancel()
        task = null
    }
}

private data class ActionBarEntry(
    val message: Component,
    val priority: Int,
    val expiresAt: Long?
) {
    val isPermanent get() = expiresAt == null
    fun isExpired() = expiresAt != null && System.currentTimeMillis() >= expiresAt
}

/**
 * Постоянный ActionBar с заданным приоритетом.
 */
fun Player.sendPermanentActionBar(message: Component, priority: Int = 1) =
    ActionBar.send(this, message, priority)

/**
 * Временный ActionBar с заданной длительностью и приоритетом.
 */
fun Player.sendTimedActionBar(message: Component, durationSec: Int, priority: Int = 1) =
    ActionBar.send(this, message, priority, durationSec * 1000L)

/**
 * Очищает все ActionBar-сообщения игрока.
 */
fun Player.clearActionBar() =
    ActionBar.clear(this)