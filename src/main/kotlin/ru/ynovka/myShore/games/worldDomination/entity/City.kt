package ru.ynovka.myShore.games.worldDomination.entity

import net.kyori.adventure.text.TranslatableComponent
import ru.ynovka.myShore.utils.Utils.intValue
import java.util.concurrent.ThreadLocalRandom


class City(
    /** Название города - ключ перевода */
    val name: TranslatableComponent,
    /** Ссылка на класс страну-родитель */
    val country: Country
) {
    /** Уровень города */
    var lvl = 0
        private set
    /** Жив ли город? */
    var isAlive = true
        private set
    var hasShield = false
        private set
    private val startCapitalization = ThreadLocalRandom.current().nextInt(250, 275)
    val capitalization
        get() = startCapitalization + lvl * 100 * isAlive.intValue

    /** Публичный апи для изменения состояние города*/

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
        const val UPGRADE_COST: Int = 1

        fun getCityById(
            country: Country,
            cityid: Int
        ): City = country.cities[cityid] ?: throw IllegalArgumentException("City not found")
    }
}

