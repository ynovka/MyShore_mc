package ru.ynovka.myShore.games.worldDomination

import com.github.darksoulq.abyssallib.world.item.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEditBookEvent
import ru.ynovka.myShore.MyShore.Companion.inst


object WDEvents : Listener{
    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler
    fun onPlayerEditBook(e: PlayerEditBookEvent) {
        Item.resolve(e.player.inventory.itemInMainHand) ?: return
        e.isSigning = false
    }
}