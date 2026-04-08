package ru.ynovka.myShore.games.tag

import ru.ynovka.myShore.text.actionBar.sendPermanentActionBar
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.states.TagWaitingForPlayers
import ru.ynovka.myShore.games.tag.states.TagVoting
import org.bukkit.scheduler.BukkitRunnable
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.hub.HubItems
import org.bukkit.entity.Player
import org.bukkit.GameMode
import ru.ynovka.myShore.text.actionBar.ComponentDecorator
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

    fun Player.setupForWaiting(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.ADVENTURE
        }
        applyVotingInventory()
        clearActivePotionEffects()
        canMove(true)

        object : BukkitRunnable() {
            val frames = arrayOf(".", "..", "...")
            var frame = 0

            override fun run() {
                if (game.fsm.current !is TagWaitingForPlayers) {
                    cancel()
                    return
                }
                if (game.findPlayer(this@setupForWaiting) == null) {
                    clearActionBar()
                    cancel()
                    return
                }

                sendPermanentActionBar(
                    ComponentDecorator.addBackground(
                        Component.translatable("bar.myshore.tag.waiting_for_players")
                            .append(Component.text(frames[frame])),
                        this@setupForWaiting
                    )
                )

                frame++
                if (frame == frames.size) frame = 0
            }
        }.runTaskTimer(inst, 0L, 10L)
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
                if (game.fsm.current !is TagVoting) {
                    cancel()
                    return
                }
                if (game.findPlayer(this@setupForVoting) == null) {
                    clearActionBar()
                    cancel()
                    return
                }

                sendPermanentActionBar(
                    ComponentDecorator.addBackground(
                        Component.translatable("bar.myshore.tag.voting")
                            .append(Component.text(frames[frame])),
                        this@setupForVoting
                    )
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