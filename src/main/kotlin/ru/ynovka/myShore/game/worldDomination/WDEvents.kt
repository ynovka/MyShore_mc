package ru.ynovka.myShore.game.worldDomination

import ru.ynovka.myShore.game.worldDomination.WDGame.Companion.currentWDGame
import ru.ynovka.myShore.game.worldDomination.states.WDUNMeeting
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import com.github.darksoulq.abyssallib.world.item.Item
import ru.ynovka.myShore.MyShore.Companion.scheduler
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import ru.ynovka.myShore.text.actionBar.ActionBar
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.plasmo.PhoneCall
import org.bukkit.event.EventPriority
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.GameMode


object WDEvents : Listener{
    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler
    fun onPlayerInteraction(e: PlayerInteractEvent) {
        if (e.player.world.name != WDWorld.WORLD_NAME || e.player.gameMode == GameMode.CREATIVE) return
        e.isCancelled = true
    }

    @EventHandler
    fun onPlayerEditBook(e: PlayerEditBookEvent) {
        Item.resolve(e.player.inventory.itemInMainHand) ?: return
        e.isSigning = false
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onPlayerCancelCall(e: PlayerDropItemEvent) {
        val phone = Item.resolve(e.player.inventory.itemInOffHand) ?: return
        if (phone.id != WDItems.wdPhoneMenu.id) return
        PhoneCall.endCall(e.player)
        e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onPlayerDropNotebook(e: PlayerDropItemEvent) {
        val book = Item.resolve(e.itemDrop.itemStack) ?: return
        if (book.id != WDItems.wdNotebook.id) return
        e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onPlayerPressF(e: PlayerSwapHandItemsEvent) {
        val phone = Item.resolve(e.player.inventory.itemInOffHand)
        if (phone != null && phone.id == WDItems.wdPhoneMenu.id) {
            PhoneCall.acceptCall(e.player)
            e.isCancelled = true
        }

        val wdGame = e.player.uniqueId.currentWDGame()
        if (wdGame != null && wdGame.fsm.current is WDUNMeeting) {
            val state = wdGame.fsm.current as WDUNMeeting
            val wdPlayer = wdGame.getOrCreatePlayer(e.player.uniqueId)

            if (state.speakingCountry != wdPlayer.country?.type?.ordinal) return
            if (e.player.uniqueId in state.nowSpeaking) return

            e.player.teleportAsync(WDUNMeeting.sceneTeleport)
            state.nowSpeaking.add(e.player.uniqueId)
            ActionBar.clear(e.player)

            e.isCancelled = true
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDismount(e: EntityDismountEvent) {
        val player = e.entity as? Player ?: return
        val wdGame = player.uniqueId.currentWDGame() ?: return
        val meeting = wdGame.fsm.current as? WDUNMeeting ?: return

        if (!meeting.isSitting(player.uniqueId)) return

        e.isCancelled = true

        scheduler.schedule {
            meeting.ensureStillSitting(player)
        }.once()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onTeleport(e: PlayerTeleportEvent) {
        val wdGame = e.player.uniqueId.currentWDGame() ?: return
        val meeting = wdGame.fsm.current as? WDUNMeeting ?: return

        if (!meeting.isSitting(e.player.uniqueId)) return

        scheduler.schedule {
            meeting.forceUnsitAfterTeleport(e.player)
        }.once()
    }
}