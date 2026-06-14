package ru.ynovka.myShore.game.pillars.gameMode


object HotbarItemsPillarsGM : PillarsGM() {
    override val giveItemsAmount: Int = 9
    override val giveItemsDelaySec: Int = 10
    override val shouldClearInventory: Boolean = true
}