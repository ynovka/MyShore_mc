package ru.ynovka.myShore.games.tag

import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.entity.Player
import ru.ynovka.myShore.hub.HubItems
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.utils.sendPermanentActionBar

/**
 * Централизованная настройка инвентаря и состояния игрока для каждого этапа игры.
 */
object TagPlayerSetup {

    /** Слот 0 — статистика, слот 8 — выход в хаб (WAITING, FINISHING) */
    fun Player.applyWaitingInventory() {
        inventory.clear()
        inventory.setItem(0, TagItems.tagPlayerStats.getStack(this))
        inventory.setItem(8, HubItems.hubTeleport.getStack(null))
    }

    /** Слот 0 — голосование, слот 1 — статистика, слот 8 — выход в хаб (VOTING) */
    fun Player.applyVotingInventory() {
        inventory.clear()
        inventory.setItem(0, TagItems.tagMapVoteMenu.getStack(null))
        inventory.setItem(1, TagItems.tagPlayerStats.getStack(this))
        inventory.setItem(8, HubItems.hubTeleport.getStack(null))
    }

    /** Слот 0 — статистика, слот 8 — выход в хаб (FINISHING) */
    fun Player.applyFinishingInventory() {
        inventory.clear()
        inventory.setItem(0, TagItems.tagPlayerStats.getStack(this))
        inventory.setItem(8, HubItems.hubTeleport.getStack(null))
    }

    /** Телепорт + gameMode + инвентарь для состояния WAITING */
    fun Player.setupForWaiting(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.ADVENTURE
        }
        applyWaitingInventory()
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
        sendPermanentActionBar(Component.translatable("bar.myshore.tag.voting"))
    }

    /** Спектатор при входе во время активной игры */
    fun Player.setupAsSpectator(game: TagGame) {
        game.map.teleport(this, game) {
            gameMode = GameMode.SPECTATOR
        }
    }
}