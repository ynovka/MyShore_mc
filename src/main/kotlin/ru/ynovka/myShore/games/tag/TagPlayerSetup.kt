package ru.ynovka.myShore.games.tag

import ru.ynovka.myShore.utils.sendPermanentActionBar
import ru.ynovka.myShore.MyShore.Companion.inst
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.hub.HubItems
import org.bukkit.entity.Player
import org.bukkit.GameMode
import org.bukkit.scheduler.BukkitRunnable


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

    fun Player.setupForWaiting(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.ADVENTURE
        }
        applyVotingInventory()
        clearActivePotionEffects()
        canMove(true)
        sendPermanentActionBar(Component.translatable("bar.myshore.tag.waiting_for_players"))
    }

    /** Телепорт + gameMode + инвентарь для состояния VOTING */
    fun Player.setupForVoting(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.ADVENTURE
        }
        applyVotingInventory()
        canMove(true)

        object : BukkitRunnable() {
            val frames = arrayOf(".", "..", "...")
            var frame = 0

            override fun run() {
                if (game.state != TagGameStates.VOTING) {
                    cancel()
                    return
                }

                sendPermanentActionBar(
                    Component.translatable("bar.myshore.tag.voting")
                        .append(Component.text(frames[frame]))
                )

                frame++
                if (frame == frames.size) frame = 0
            }
        }.runTaskTimer(inst, 0L, 10L)
    }

    /** Спектатор при входе во время активной игры */
    fun Player.setupAsSpectator(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.SPECTATOR
        }
    }
}