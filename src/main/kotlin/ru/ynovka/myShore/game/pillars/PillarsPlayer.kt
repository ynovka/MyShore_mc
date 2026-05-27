package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.GamePlayer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.UUID


class PillarsPlayer(
    playerId: UUID
) : GamePlayer(playerId) {

    private val eliminated = AtomicBoolean(false)

    var kills: Int = 0
        private set

    val isEliminated: Boolean
        get() = eliminated.get()

    fun addKill() {
        kills++
    }

    fun markEliminated(): Boolean =
        eliminated.compareAndSet(false, true)

    fun resetEliminated() {
        eliminated.set(false)
    }

    fun resetKills() {
        kills = 0
    }
}
