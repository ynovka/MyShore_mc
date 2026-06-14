package ru.ynovka.myShore.game.pillars.gameMode

import ru.ynovka.myShore.game.pillars.PillarsGame


abstract class PillarsGM {
    open fun roundStart(game: PillarsGame) { }

    open val giveItemsAmount: Int = 1
    open val giveItemsDelaySec: Int = 5
    open val shouldClearInventory: Boolean = false
}

enum class PillarsGameMode(
    val gm: PillarsGM
) {
    NULL(NullPillarsGM),
    HOTBAR_ITEMS(HotbarItemsPillarsGM),
    LAVA_RUSH(LavaRushPillarsGM),
}
