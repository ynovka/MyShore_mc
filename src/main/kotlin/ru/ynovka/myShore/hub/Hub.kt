package ru.ynovka.myShore.hub

import ru.ynovka.myShore.utils.PlayerVisibilityController
import ru.ynovka.myShore.utils.Utils.clearTeams
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.GameMode
import org.bukkit.Bukkit
import org.bukkit.World
import ru.ynovka.myShore.games.GameManager
import ru.ynovka.myShore.text.ActionBarController
import ru.ynovka.myShore.utils.canMove


object Hub {
    val world: World = Bukkit.getWorld("hub")!!
    val spawn: Location = Location(world, 0.5 , 100.0, 0.5)

    fun Player.toHub() {
        teleportAsync(spawn)
        clearActivePotionEffects()
        applyHubInventory()
        GameManager.leave(this)
        PlayerVisibilityController.refreshAll()
        clearTeams()
        ActionBarController.clear(this)
        canMove(true)
        gameMode = GameMode.ADVENTURE
        saturation = 20f
        health = 20.0
    }

    private fun Player.applyHubInventory() {
        val inv = this.inventory
        inv.clear()
        inv.setItem(0, HubItems.playMenu.getStack(null))
    }
}