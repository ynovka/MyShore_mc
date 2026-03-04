package ru.ynovka.myShore.lobby

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore

object PlayerVisibilityController {

    /**
     * Основная функция. Вызывать, когда у target меняется статус (вход, выход, смена лобби).
     * Она обновляет видимость ДЛЯ игрока и видимость ИГРОКА для остальных.
     */
    fun refreshVisibility(target: Player) {
        val targetLobby = target.getLobby()

        for (other in Bukkit.getOnlinePlayers()) {
            if (other.uniqueId == target.uniqueId) continue

            val otherLobby = other.getLobby()

            // 1. Настраиваем, кого видит target
            if (shouldSee(targetLobby, otherLobby)) {
                target.showPlayer(MyShore.Companion.inst, other)
            } else {
                target.hidePlayer(MyShore.Companion.inst, other)
            }

            // 2. Настраиваем, кто видит target (обновляем зрение остальных игроков)
            if (shouldSee(otherLobby, targetLobby)) {
                other.showPlayer(MyShore.Companion.inst, target)
            } else {
                other.hidePlayer(MyShore.Companion.inst, target)
            }
        }
    }

    /**
     * Логика видимости:
     * Игрок видит другого, ТОЛЬКО если они находятся в одном и том же лобби.
     * (null == null) -> Игроки в хабе видят друг друга.
     */
    private fun shouldSee(viewerLobby: Lobby?, targetLobby: Lobby?): Boolean {
        return viewerLobby == targetLobby
    }
}