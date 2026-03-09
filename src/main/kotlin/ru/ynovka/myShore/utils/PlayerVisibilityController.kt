package ru.ynovka.myShore.utils

import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.lobby.getLobby
import ru.ynovka.myShore.lobby.Lobby
import org.bukkit.entity.Player
import org.bukkit.Bukkit


object PlayerVisibilityController {
    fun refreshVisibility(target: Player) {
        val targetLobby = target.getLobby()

        for (other in Bukkit.getOnlinePlayers()) {
            if (other.uniqueId == target.uniqueId) continue

            val otherLobby = other.getLobby()

            updatePair(target, targetLobby, other, otherLobby)
        }
    }

    fun refreshAll() {
        for (player in Bukkit.getOnlinePlayers()) {
            refreshVisibility(player)
        }
    }

    private fun updatePair(
        p1: Player,
        lobby1: Lobby?,
        p2: Player,
        lobby2: Lobby?
    ) {
        if (shouldSee(lobby1, lobby2)) {
            p1.showPlayer(inst, p2)
        } else {
            p1.hidePlayer(inst, p2)
        }

        if (shouldSee(lobby2, lobby1)) {
            p2.showPlayer(inst, p1)
        } else {
            p2.hidePlayer(inst, p1)
        }
    }

    private fun shouldSee(viewerLobby: Lobby?, targetLobby: Lobby?): Boolean {
        return viewerLobby == targetLobby
    }
}