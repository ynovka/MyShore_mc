package ru.ynovka.myShore.hub

import ru.ynovka.myShore.utils.Utils.clearTeams
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.GameMode
import org.bukkit.Bukkit
import ru.ynovka.myShore.visibilityGroup.VisibilityGroup
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.text.actionBar.ActionBar
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.utils.restrictToBlock
import ru.ynovka.myShore.utils.restrictToBlockCenter


object Hub {
    val world by lazy { Bukkit.getWorld("hub")!! }
    val spawn by lazy { Location(world, 0.5 , 100.0, 0.5, 90f, 0f) }
    val hubVisibilityGroup = VisibilityGroup()

    fun Player.toHub() {
        ru.ynovka.myShore.MyShore.scheduler.schedule {
            hubVisibilityGroup.addViewer(uniqueId)
            // TODO teleportAsync(spawn)
            PillarsGame.hubWorld.teleportToSpawn(this) // todo tmp
            clearActivePotionEffects()
            applyHubInventory()
            GameManager.leave(this.uniqueId)
            clearTeams()
            ActionBar.clear(this)
            restrictToBlock(false)
            restrictToBlockCenter(false)
            gameMode = GameMode.ADVENTURE
            saturation = 20f
            health = 20.0
        }
            .entity(this)
            .once()
    }

    private fun Player.applyHubInventory() {
        inventory.clear()
        // TODO inventory.setItem(0, HubItems.playMenu.getStack(null))
    }
}