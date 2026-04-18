package ru.ynovka.myShore.games.worldDomination.entity

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.mm
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDPlayerRole
import ru.ynovka.myShore.texturepack.Glyphs
import ru.ynovka.myShore.visibilityGroup.VisibilityGroup


class Country private constructor(
    /** Ссылка на игру */
    val game: WDGame,
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

    /** Жива ли страна? */
    val isAlive
        get() = cities.any { it.value.isAlive }

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
        president.role = WDPlayerRole.PRESIDENT

        // Собираем города из пресетов — this уже существует
        type.cityPresets.forEachIndexed { index, preset ->
            cities[index] = City(preset.name, this, preset.capitalizationRange)
        }
    }

    fun teleport(player: Player) {
        // countryVisibilityGroup.addViewer(player.uniqueId) todo раскоментить
        player.teleportAsync(skin.loc)
    }

    /** Вызывается в начале этапа переговоров */
    fun onStartNewRound() {
        collectRoundProfit()
        bombsAvailable = bombsMaking
        bombsMaking = 0
    }

    /** Сбор прибыли за раунд */
    fun collectRoundProfit() {
        if (game.round == 0) return
        balance += cities.values.sumOf { it.capitalization }
    }

    /** @return true если ядерная технология успешно изучена */
    fun learnNuclear(): Boolean {
        if (isNuclearLearned) return false
        if (balance < LEARN_NUCLEAR_COST) return false
        balance -= LEARN_NUCLEAR_COST
        isNuclearLearned = true
        return true
    }

    /** @return true если ядерная бомба успешно создана */
    fun craftNuclearBomb(): Boolean {
        if (balance < CRAFT_NUCLEAR_BOMB_COST) return false
        balance -= CRAFT_NUCLEAR_BOMB_COST
        bombsMaking += 1
        return true
    }

    /** @return true если вклад в экологию успешно сделан */
    fun investmentsEcology(): Boolean {
        if (balance < ECOLOGY_COST) return false
        balance -= ECOLOGY_COST
        game.ecology += 1
        return true
    }

    fun setVicePresident(player: WDPlayer) {
        addCitizen(player)
        president.role = WDPlayerRole.VICE_PRESIDENT
        vicePresident = player
    }

    fun addCitizen(player: WDPlayer) {
        citizens.add(player)
        president.role = WDPlayerRole.CITIZEN
        player.country = this
    }

    /* todo Нужно подумать над ливом игроков во время катки fun removeCitizen(player: WDPlayer) {
        citizens.remove(player)
        player.country = null
    }*/

    companion object {
        const val LEARN_NUCLEAR_COST: Int = 150
        const val CRAFT_NUCLEAR_BOMB_COST: Int = 250
        const val ECOLOGY_COST: Int = 50

        /** Единственная точка создания Country */
        fun create(game: WDGame, president: WDPlayer, type: CountryType): Country =
            Country(game, president, type)

        fun Country?.getFlag(): String {
            return Glyphs.COUNTRY_FLAGS[this?.type?.name] ?: ""
        }

        fun Country.getFormattedName(player: Player) =
            mm.deserialize("<white>${getFlag()} ")
                .append(ServerTranslator.translate(type.nameTranslatable, player))

        fun WDPlayer.getFormattedName() = mm.deserialize("${country.getFlag()} ${player.name}")

    }
}