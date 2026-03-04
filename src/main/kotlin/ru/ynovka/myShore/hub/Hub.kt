package ru.ynovka.myShore.hub

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import ru.ynovka.myShore.hub.menus.PlayMenu
import ru.ynovka.myShore.lobby.LobbyManager

object Hub {
    val world: World = Bukkit.getWorld("hub")!!
    val spawn: Location = Location(world, 0.5 , 100.0, 0.5)

    init {
        PlayMenu
    }

    fun Player.toHub() {
        this.teleportAsync(spawn)
        this.clearActivePotionEffects()
        this.applyHubInventory()
        LobbyManager.leave(this)
        this.saturation = 20f
        this.health = 20.0
    }

    private fun Player.applyHubInventory() {
        val inv = this.inventory
        inv.clear()
        inv.setItem(0, HubItems.playMenu.getStack(null))
    }
}