package ru.ynovka.myShore.games.worldDomination.entity

import net.kyori.adventure.text.TranslatableComponent
import ru.ynovka.myShore.utils.Utils.intValue
import java.util.concurrent.ThreadLocalRandom

class City(
    /** Название города - ключ перевода */
    val name: TranslatableComponent,
    /** Ссылка на страну-родитель */
    val country: Country,
    capitalizationRange: IntRange = 250..275
) {
    /** Уровень города */
    var lvl = 0
        private set

    /** Жив ли город? */
    var isAlive = true
        private set

    var hasShield = false
        private set

    private val startCapitalization = ThreadLocalRandom.current()
        .nextInt(capitalizationRange.first, capitalizationRange.last)

    val capitalization
        get() = startCapitalization + lvl * 100 * isAlive.intValue

    /** Бомбить город */
    fun bombardCity() {
        if (hasShield) {
            hasShield = false
            return
        }
        isAlive = false
    }

    /** @return true если щит успешно установлен */
    fun buyShield(): Boolean {
        if (hasShield) return false
        if (country.balance < 150) return false
        country.balance -= 150
        hasShield = true
        return true
    }

    /** @return true если город успешно улучшен */
    fun buyUpgrade(): Boolean {
        if (country.balance < 250) return false
        country.balance -= 250
        lvl += 1
        return true
    }

    companion object {
        const val UPGRADE_COST: Int = 250

        fun getCityById(country: Country, cityId: Int): City =
            country.cities[cityId] ?: throw IllegalArgumentException("City $cityId not found")
    }
}