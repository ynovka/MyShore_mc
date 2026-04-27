package ru.ynovka.myShore.games.worldDomination

import com.github.darksoulq.abyssallib.world.item.Item
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.worldDomination.WDGame.Companion.currentWDGame
import ru.ynovka.myShore.games.worldDomination.states.WDUNMeeting
import ru.ynovka.myShore.plasmo.PhoneCall
import ru.ynovka.myShore.text.actionBar.ActionBar


object WDEvents : Listener{
    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler
    fun onPlayerInteraction(e: PlayerInteractEvent) {
        if (e.player.world.name != WDGame.world.name || e.player.gameMode == GameMode.CREATIVE) return
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

        val wdGame = e.player.currentWDGame()
        if (wdGame != null && wdGame.fsm.current is WDUNMeeting) {
            val state = wdGame.fsm.current as WDUNMeeting
            val wdPlayer = wdGame.getOrCreatePlayer(e.player)

            if (state.speakingCountry != wdPlayer.country?.type?.ordinal) return
            if (e.player.uniqueId in state.nowSpeaking) return

            e.player.teleportAsync(WDUNMeeting.sceneTeleport)
            state.nowSpeaking.add(e.player.uniqueId)
            ActionBar.clear(e.player)

            e.isCancelled = true
        }
    }
}