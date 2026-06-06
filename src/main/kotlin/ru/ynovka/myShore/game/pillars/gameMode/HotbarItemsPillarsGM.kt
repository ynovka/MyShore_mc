package ru.ynovka.myShore.game.pillars.gameMode

import ru.ynovka.myShore.game.pillars.states.PillarsInProgress.Companion.items
import org.bukkit.inventory.ItemStack
import org.bukkit.entity.Player


object HotbarItemsPillarsGM : PillarsGM {
    override fun onGiveRandomItems(player: Player) {
        player.inventory.clear()
        repeat(9) {
            player.inventory.addItem(ItemStack.of(items.random()))
        }
    }
}