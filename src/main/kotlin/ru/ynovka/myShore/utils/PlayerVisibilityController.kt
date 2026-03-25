package ru.ynovka.myShore.utils

import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameManager.currentGame


object PlayerVisibilityController {
    fun refreshVisibility(target: Player) {
        val targetGame = target.currentGame()

        for (other in Bukkit.getOnlinePlayers()) {
            if (other.uniqueId == target.uniqueId) continue

            val otherGame = other.currentGame()

            updatePair(target, targetGame, other, otherGame)
        }
    }

    fun refreshAll() {
        for (player in Bukkit.getOnlinePlayers()) {
            refreshVisibility(player)
        }
    }

    private fun updatePair(
        p1: Player,
        game1: Game<*>?,
        p2: Player,
        game2: Game<*>?
    ) {
        if (shouldSee(game1, game2)) {
            p1.showPlayer(inst, p2)
        } else {
            p1.hidePlayer(inst, p2)
        }

        if (shouldSee(game2, game1)) {
            p2.showPlayer(inst, p1)
        } else {
            p2.hidePlayer(inst, p1)
        }
    }

    private fun shouldSee(viewerGame: Game<*>?, targetGame: Game<*>?): Boolean {
        return viewerGame == targetGame
    }
}