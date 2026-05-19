package ru.ynovka.myShore.plasmo

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore
import ru.ynovka.myShore.text.actionBar.clearActionBar
import ru.ynovka.myShore.text.actionBar.sendTimedActionBar
import ru.ynovka.myShore.utils.Utils.asPlayer
import java.time.Duration
import java.util.UUID

private const val CALL_DURATION_TICKS = 30 * 20L

object PhoneCall {

    private val pendingCalls: MutableList<Call> = mutableListOf()
    private val calls: MutableList<Call> = mutableListOf()

    fun getActiveCallForPlayer(uuid: UUID): Call? =
        calls.firstOrNull { it.from == uuid || it.to == uuid }

    /**
     * Инициирует звонок от [from] к [to].
     * Не выполняется, если звонящий уже в звонке или получатель занят.
     */
    fun call(
        from: Player,
        fromName: Component,
        to: Player,
        toName: Component,
        onEnd: ((Call) -> Unit)? = null,
        onSuccessEnd: ((Call) -> Unit)? = null
    ) {
        if (pendingCalls.any { it.from == from.uniqueId } ||
            calls.any { it.from == from.uniqueId || it.to == from.uniqueId }) return

        if (pendingCalls.any { it.to == to.uniqueId } ||
            calls.any { it.from == to.uniqueId || it.to == to.uniqueId }) {
            from.sendTimedActionBar(
                Component.translatable("bar.myshore.wd.call_target_is_busy"),
                5
            )
            return
        }

        pendingCalls += Call(
            from = from.uniqueId,
            fromName = fromName,
            to = to.uniqueId,
            toName = toName,
            onEnd = onEnd,
            onSuccessEnd = onSuccessEnd
        )

        from.sendTimedActionBar(
            Component.translatable("bar.myshore.wd.call_waiting", toName),
            10
        )
        to.showTitle(
            Title.title(
                Component.text(""),
                Component.translatable("sub.title.myshore.wd.incoming_call", fromName),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(9), Duration.ofMillis(500))
            )
        )
        to.sendTimedActionBar(
            Component.translatable("bar.myshore.wd.call_control"),
            10
        )
    }

    /**
     * Принимает входящий звонок для [player].
     */
    fun acceptCall(player: Player) {
        val call = pendingCalls.firstOrNull { it.to == player.uniqueId } ?: return
        val fromPlayer = call.from.asPlayer()

        pendingCalls -= call

        if (fromPlayer == null) {
            player.clearTitle()
            player.clearActionBar()
            return
        }

        val activeCall = call.copy(startTime = System.currentTimeMillis())
        calls += activeCall

        player.clearTitle()
        player.clearActionBar()
        fromPlayer.clearActionBar()

        PhoneCallVoice.startCallAudio(MyShore.plasmo, activeCall)

        MyShore.scheduler.schedule {
            if (calls.contains(activeCall)) {
                terminateCall(activeCall)
                activeCall.from.asPlayer()?.sendTimedActionBar(
                    Component.translatable("bar.myshore.wd.call_timeout"), 3
                )
                activeCall.to.asPlayer()?.sendTimedActionBar(
                    Component.translatable("bar.myshore.wd.call_timeout"), 3
                )
            }
        }
            .after(CALL_DURATION_TICKS, Clock.TICKS)
            .once()
    }

    fun endCall(player: Player) {
        val uuid = player.uniqueId

        // 1. Исходящий pending (игрок сам звонил → отмена)
        val outgoingPending = pendingCalls.firstOrNull { it.from == uuid }
        if (outgoingPending != null) {
            pendingCalls -= outgoingPending

            player.sendTimedActionBar(
                Component.translatable("bar.myshore.wd.call_cancelled"),
                3
            )

            outgoingPending.to.asPlayer()?.clearTitle()
            outgoingPending.to.asPlayer()?.clearActionBar()

            outgoingPending.onEnd?.invoke(outgoingPending)
            return
        }

        // 2. Входящий pending (игроку звонят → отклонение)
        val incomingPending = pendingCalls.firstOrNull { it.to == uuid }
        if (incomingPending != null) {
            pendingCalls -= incomingPending

            player.clearTitle()
            player.clearActionBar()

            incomingPending.from.asPlayer()?.sendTimedActionBar(
                Component.translatable("bar.myshore.wd.call_denied"),
                3
            )

            incomingPending.onEnd?.invoke(incomingPending)
            return
        }

        // 3. Активный звонок → завершение
        val activeCall = calls.firstOrNull { it.from == uuid || it.to == uuid } ?: return
        val otherUuid = if (activeCall.from == uuid) activeCall.to else activeCall.from

        terminateCall(activeCall)
        activeCall.onEnd?.invoke(activeCall)
        activeCall.onSuccessEnd?.invoke(activeCall)

        player.sendTimedActionBar(
            Component.translatable("bar.myshore.wd.call_hang_up.self"),
            3
        )
        otherUuid.asPlayer()?.sendTimedActionBar(
            Component.translatable("bar.myshore.wd.call_hang_up.other"),
            3
        )
    }

    fun endAllCalls(players: Collection<UUID>) {
        if (players.isEmpty()) return

        val playerSet = players.toHashSet()

        // 1. Завершаем активные звонки
        val toEnd = calls.filter { call ->
            call.from in playerSet || call.to in playerSet
        }

        for (call in toEnd) {
            terminateCall(call)

            call.from.asPlayer()?.sendTimedActionBar(
                Component.translatable("bar.myshore.wd.call_ended"),
                3
            )
            call.to.asPlayer()?.sendTimedActionBar(
                Component.translatable("bar.myshore.wd.call_ended"),
                3
            )

            call.onEnd?.invoke(call)
        }

        // 2. Удаляем pending звонки
        val pendingToRemove = pendingCalls.filter { call ->
            call.from in playerSet || call.to in playerSet
        }

        for (call in pendingToRemove) {
            pendingCalls -= call

            call.from.asPlayer()?.clearTitle()
            call.to.asPlayer()?.clearTitle()

            call.onEnd?.invoke(call)
        }
    }

    private fun terminateCall(call: Call) {
        calls -= call
        PhoneCallVoice.stopCallAudio(call)
    }
}

data class Call(
    val from: UUID,
    val fromName: Component,
    val to: UUID,
    val toName: Component,
    val startTime: Long? = null,
    val onEnd: ((Call) -> Unit)? = null,
    val onSuccessEnd: ((Call) -> Unit)? = null,
)