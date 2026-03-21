package ru.ynovka.myShore.games.worldDomination.entity

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import java.util.UUID

class Country(
    /** Президент */
    val president: UUID,
    /** Описание страны */
    val type: Countrytype
) {
    /** Вице-президент */
    var vicePresident: UUID? = null
        private set
    /** Список игроков страны */
    val citizens: MutableList<UUID> = mutableListOf(president)
    /** Баланс госудаства */
    var balance: Int = 950
    /** Изучена ли ядерная технология */
    var isNuclearLearned: Boolean = false
        private set
    /** Бомбы, доступные для использования */
    var bombsAvailable: Int = 0
        private set
    /** Бомбы, в процессе создания */
    var bombsMaking: Int = 0
        private set
    /** Города страны */
    val cities = mutableMapOf<Int, City>()

    fun collectRoundProfit() {
        var profit = 0
        cities.values.forEach { profit += it.capitalization }
        balance += profit
    }
}

// Список стран для игры доступных
enum class Countrytype(
    name: TranslatableComponent,
    cities: List<City>
) {
    RUSSIA(
        Component.translatable("name.myshore.wd.country.russia"),
        listOf(

        )
    )
}