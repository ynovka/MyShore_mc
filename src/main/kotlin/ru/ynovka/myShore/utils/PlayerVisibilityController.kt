package ru.ynovka.myShore.utils

import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.lobby.getLobby
import ru.ynovka.myShore.lobby.Lobby
import org.bukkit.entity.Player
import org.bukkit.Bukkit


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
                target.showPlayer(inst, other)
            } else {
                target.hidePlayer(inst, other)
            }

            // 2. Настраиваем, кто видит target (обновляем зрение остальных игроков)
            if (shouldSee(otherLobby, targetLobby)) {
                other.showPlayer(inst, target)
            } else {
                other.hidePlayer(inst, target)
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

    /**
     * Очистка не требуется, так как мы не храним Map, но метод можно оставить пустым
     * для совместимости с вашими эвентами.
     */
    fun removePlayer(player: Player) {
        // Ничего не делаем, state-less подход
    }
}