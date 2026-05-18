package ru.ynovka.myShore.games.tag

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.text.actionBar.sendPermanentActionBar
import ru.ynovka.myShore.games.tag.states.TagWaitingForPlayers
import ru.ynovka.myShore.games.tag.states.TagVoting
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.hub.HubItems
import org.bukkit.entity.Player
import org.bukkit.GameMode
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

    fun Player.setupForWaiting(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.ADVENTURE
        }
        applyVotingInventory()
        clearActivePotionEffects()
        canMove(true)

        val frames = arrayOf(".", "..", "...")
        var frame = 0
        var active = true

        game.scheduler.schedule {
            if (game.fsm.current !is TagWaitingForPlayers) {
                active = false
                return@schedule
            }
            if (game.findPlayer(this@setupForWaiting) == null) {
                clearActionBar()
                active = false
                return@schedule
            }

            sendPermanentActionBar(
                ComponentDecorator.addBackground(
                    Component.translatable("bar.myshore.waiting_for_players")
                        .append(Component.text(frames[frame])),
                    this@setupForWaiting
                )
            )

            frame++
            if (frame == frames.size) frame = 0
        }
            .sync()
            .repeatWhile { active }
            .repeatEvery(10L, Clock.TICKS)
    }

    /** Телепорт + gameMode + инвентарь для состояния VOTING */
    fun Player.setupForVoting(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.ADVENTURE
        }
        applyVotingInventory()
        canMove(true)

        val frames = arrayOf(".", "..", "...")
        var frame = 0
        var active = true

        game.scheduler.schedule {
            if (game.fsm.current !is TagVoting) {
                active = false
                return@schedule
            }
            if (game.findPlayer(this@setupForVoting) == null) {
                clearActionBar()
                active = false
                return@schedule
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
            .sync()
            .repeatWhile { active }
            .repeatEvery(10L, Clock.TICKS)
    }

    /** Спектатор при входе во время активной игры */
    fun Player.setupAsSpectator(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.SPECTATOR
        }
    }
}