package ru.ynovka.myShore.hub

import ru.ynovka.myShore.utils.Utils.clearTeams
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.GameMode
import org.bukkit.Bukkit
import org.bukkit.GameRule
import ru.ynovka.myShore.visibilityGroup.VisibilityGroup
import ru.ynovka.myShore.games.GameManager
import ru.ynovka.myShore.text.actionBar.ActionBar
import ru.ynovka.myShore.utils.canMove


object Hub {
    val world by lazy { Bukkit.getWorld("hub")!! }
    val spawn by lazy { Location(world, 0.5 , 100.0, 0.5, 90f, 0f) }
    val hubVisibilityGroup = VisibilityGroup()

    fun Player.toHub() {
        hubVisibilityGroup.addViewer(uniqueId)
        teleportAsync(spawn)
        clearActivePotionEffects()
        applyHubInventory()
        GameManager.leave(this)
        clearTeams()
        ActionBar.clear(this)
        canMove(true)
        gameMode = GameMode.ADVENTURE
        saturation = 20f
        health = 20.0
    }

    private fun Player.applyHubInventory() {
        inventory.clear()
        inventory.setItem(0, HubItems.playMenu.getStack(null))
    }
}