package ru.ynovka.myShore.games.worldDomination

import com.github.darksoulq.abyssallib.world.item.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerEditBookEvent
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.plasmo.PhoneCall


object WDEvents : Listener{
    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
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
}