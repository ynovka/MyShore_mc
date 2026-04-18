package ru.ynovka.myShore.games.worldDomination.entity

import net.kyori.adventure.text.TranslatableComponent
import ru.ynovka.myShore.utils.Utils.intValue

class City(
    /** Название города - ключ перевода */
    val name: TranslatableComponent,
    /** Ссылка на страну-родитель */
    val country: Country,
    private val startCapitalization: Int
) {
    /** Уровень города */
    var lvl = 0
        private set

    /** Жив ли город? */
    var isAlive = true
        private set

    var hasShield = false
        private set

    val capitalization
        get() = (startCapitalization + lvl * 100) * isAlive.intValue

    /** Бомбить город */
    fun bombardCity() {
        if (hasShield) {
            hasShield = false
            return
        }
        isAlive = false
        country.game.ecology -= ECOLOGY_DESTROY_PENALTY
    }

    /** @return true если щит успешно установлен */
    fun buyShield(): Boolean {
        if (hasShield) return false
        if (country.balance < SHIELD_COST) return false
        country.balance -= SHIELD_COST
        hasShield = true
        return true
    }

    /** @return true если город успешно улучшен */
    fun buyUpgrade(): Boolean {
        if (country.balance < UPGRADE_COST) return false
        country.balance -= UPGRADE_COST
        lvl += 1
        return true
    }

    companion object {
        const val UPGRADE_COST: Int = 150
        const val SHIELD_COST: Int = 300
        const val ECOLOGY_DESTROY_PENALTY: Double = 0.05
    }
}