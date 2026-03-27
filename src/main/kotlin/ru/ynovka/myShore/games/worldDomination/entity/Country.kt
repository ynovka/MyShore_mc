package ru.ynovka.myShore.games.worldDomination.entity

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.Bukkit
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import java.time.Duration
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toKotlinDuration

class Country(
    /** Президент */
    val president: WDPlayer,
    /** Описание страны */
    val type: CountryType
) {
    /** Вице-президент */
    var vicePresident: WDPlayer? = null
        private set
    /** Список игроков страны */
    val citizens: MutableList<WDPlayer> = mutableListOf(president)
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

    init { addCitizen(president) }

    fun addCitizen(player: WDPlayer) {
        citizens.add(player)
        player.country = this
    }

    fun removeCitizen(player: WDPlayer) {
        citizens.remove(player)
        player.country = null
    }

    companion object {
        val pendingInvitesToVicePresident: MutableMap<Country, WDPlayer> = mutableMapOf()

        fun inviteVicePersident(
            country: Country,
            target: WDPlayer
        ) {
            // Отслыаем сообщение в чате у цели:
            // "Президент X страны Y предлагает вам должность вице-призедента"
            // "|-> ПРИНЯТЬ | ОТКЛОНИТЬ"

            // Через 15 секунд удаляем приглашение
            inst.server.scheduler.runTaskLater(inst, Runnable {

                // Пишем в чат президенту: "target не принял ваше приглашение"
                // Пишем в чат target: "Вы не успели принять приглашение от "
            }, 15 * 20L)
        }
    }
}

// Список стран для игры доступных
enum class CountryType(
    name: TranslatableComponent,
    cities: List<City>
) {
    RUSSIA(
        Component.translatable("name.myshore.wd.country.russia"),
        listOf(

        )
    )
}