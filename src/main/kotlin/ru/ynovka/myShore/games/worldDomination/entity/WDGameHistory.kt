package ru.ynovka.myShore.games.worldDomination.entity

enum class WDAction {
    BOMBARDMENT,
    NUCLEAR_BOMB_CREATED,
    NUCLEAR_LEARNED,
    ECOLOGY_INVESTED,
    SHIELD_BUILT,
    CITY_UPGRADED,
    SANCTION,
    SPY
}

data class WDHistoryEntry(
    val action: WDAction,
    val round: Int,
    val actor: Country,
    val targetCountry: Country? = null,
    val targetCity: City? = null
)

class WDGameHistory {
    private val entries: MutableList<WDHistoryEntry> = mutableListOf()

    fun record(entry: WDHistoryEntry) {
        entries += entry
    }

    /**
     * Возвращает сгруппированную статистику действий страны за текущий и предыдущий раунд.
     * Ключ — тип действия, значение — количество раз.
     */
    fun getActionsOf(country: Country, currentRound: Int): Map<WDAction, Int> =
        entries
            .filter { it.actor == country && it.round >= currentRound - 1 }
            .groupingBy { it.action }
            .eachCount()
}