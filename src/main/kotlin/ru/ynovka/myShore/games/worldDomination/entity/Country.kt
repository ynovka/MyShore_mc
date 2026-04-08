package ru.ynovka.myShore.games.worldDomination.entity

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.visibilityGroup.VisibilityGroup
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.texturepack.Glyphs


class Country private constructor(
    /** Президент */
    val president: WDPlayer,
    /** Пресет страны */
    val type: CountryType
) {
    /** Скин страны */
    val skin = CountrySkin.get(president)

    /** Вице-президент */
    var vicePresident: WDPlayer? = null
        private set

    /** Список игроков страны */
    val citizens: MutableList<WDPlayer> = mutableListOf()

    /** Баланс государства */
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

    val countryVisibilityGroup = VisibilityGroup()

    init {
        // Добавляем президента
        addCitizen(president)

        // Собираем города из пресетов — this уже существует
        type.cityPresets.forEachIndexed { index, preset ->
            cities[index] = City(preset.name, this, preset.capitalizationRange)
        }
    }

    fun teleport(player: Player) {
        // countryVisibilityGroup.addViewer(player.uniqueId) todo раскоментить
        player.teleportAsync(skin.loc)
    }

    fun collectRoundProfit() {
        balance += cities.values.sumOf { it.capitalization }
    }

    fun setVicePresident(player: WDPlayer) {
        addCitizen(player)
        vicePresident = player
    }

    fun addCitizen(player: WDPlayer) {
        citizens.add(player)
        player.country = this
    }

    fun removeCitizen(player: WDPlayer) {
        citizens.remove(player)
        player.country = null
    }

    companion object {
        /** Единственная точка создания Country */
        fun create(president: WDPlayer, type: CountryType): Country =
            Country(president, type)

        val pendingInvitesToVicePresident: MutableMap<Country, WDPlayer> = mutableMapOf()

        fun inviteVicePresident(country: Country, target: WDPlayer) {
            // Отправляем сообщение в чате у цели:
            // "Президент X страны Y предлагает вам должность вице-президента"
            // "|-> ПРИНЯТЬ | ОТКЛОНИТЬ"
            inst.server.scheduler.runTaskLater(inst, Runnable {
                // Пишем в чат президенту: "target не принял ваше приглашение"
                // Пишем в чат target: "Вы не успели принять приглашение"
            }, 15 * 20L)
        }

        fun Country?.getFlag(): String {
            return Glyphs.COUNTRY_FLAGS[this?.type?.name] ?: ""
        }

        fun WDPlayer.getFormattedName() = MiniMessage.miniMessage().deserialize("${country.getFlag()} ${player.name}")

    }
}