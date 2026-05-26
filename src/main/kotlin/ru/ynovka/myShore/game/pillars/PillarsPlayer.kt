package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.GamePlayer
import java.util.UUID


class PillarsPlayer(
    playerId: UUID
) : GamePlayer(playerId) {

    var kills: Int = 0
        private set

    fun addKill() {
        kills++
    }

    fun resetKills() {
        kills = 0
    }
}