package ru.ynovka.myShore.game.tag

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.text.actionBar.sendPermanentActionBar
import ru.ynovka.myShore.game.tag.states.TagWaitingForPlayers
import ru.ynovka.myShore.game.tag.states.TagVoting
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.hub.HubItems
import org.bukkit.entity.Player
import org.bukkit.GameMode
import ru.ynovka.myShore.game.gameUtils.ActionbarWaitingFor
import ru.ynovka.myShore.text.ComponentDecorator
import ru.ynovka.myShore.text.actionBar.clearActionBar


/**
 * Централизованная настройка инвентаря и состояния игрока для каждого этапа игры.
 */
object TagPlayerSetup {
    fun Player.applyVotingInventory() {
        inventory.clear()
        inventory.setItem(0, TagItems.tagMapVoteMenu.getStack(null))
        inventory.setItem(8, HubItems.hubTeleport.getStack(null))
        inventory.setItem(9, TagItems.tagPlayerStats.getStack(this))
    }

    fun Player.applyInProgressInventory() {
        inventory.clear()
        inventory.setItem(9, TagItems.tagPlayerStats.getStack(this))
    }

    fun Player.applyFinishingInventory() {
        inventory.clear()
        inventory.setItem(8, HubItems.hubTeleport.getStack(null))
        inventory.setItem(9, TagItems.tagPlayerStats.getStack(this))
    }

    fun Player.setupForWaitingOrVoting(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.ADVENTURE
        }
        applyVotingInventory()
        clearActivePotionEffects()
        canMove(true)
    }
}