package ru.ynovka.myShore.game.pillars.gameMode

import org.bukkit.entity.Player
import ru.ynovka.myShore.game.pillars.states.PillarsInProgress.Companion.items
import ru.ynovka.myShore.game.pillars.PillarsGame
import org.bukkit.inventory.ItemStack


interface PillarsGM {
    fun roundStart(game: PillarsGame) { }

    fun onGiveRandomItems(player: Player) {
        player.inventory.addItem(ItemStack.of(items.random()))
    }
}

enum class PillarsGameMode(
    val gm: PillarsGM
) {
    NULL(NullPillarsGM),
    HOTBAR_ITEMS(HotbarItemsPillarsGM),
    LAVA_RUSH(LavaRushPillarsGM),
}
