package ru.ynovka.myShore.hub

import ru.ynovka.myShore.game.gameUtils.VisibilityGroup
import ru.ynovka.myShore.utils.restrictToBlockCenter
import ru.ynovka.myShore.utils.Utils.clearTeams
import ru.ynovka.myShore.utils.restrictToBlock
import ru.ynovka.myShore.game.GameManager
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.GameMode
import org.bukkit.Bukkit


object Hub {
    val world by lazy { Bukkit.getWorld("hub")!! }
    val spawn by lazy { Location(world, 4.5 , 100.0, 4.5, -90f, 0f) }
    val hubVisibilityGroup = VisibilityGroup()

    fun Player.toHub() {
        ru.ynovka.myShore.MyShore.scheduler.schedule {
            gameMode = GameMode.ADVENTURE
            saturation = 20f
            foodLevel = 20
            health = 20.0
            clearActivePotionEffects()
            teleportAsync(spawn)
            applyHubInventory()
            clearTeams()

            restrictToBlock(false)
            restrictToBlockCenter(false)
            GameManager.leave(this.uniqueId)
            hubVisibilityGroup.addViewer(uniqueId)
        }
            .entity(this)
            .once()
    }

    private fun Player.applyHubInventory() {
        inventory.clear()
    }
}