package ru.ynovka.myShore.games.worldDomination.entity

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.mm
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDPlayerRole
import ru.ynovka.myShore.texturepack.Glyphs
import ru.ynovka.myShore.visibilityGroup.VisibilityGroup
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.ceil


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

    val levelOfLife: Int
        get() = ceil(cities.values.sumOf { it.capitalization } * game.ecology / 4.0).toInt()

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

    /** Бомбардировки, отложенные до конца этапа переговоров */
    val pendingBombardments: MutableList<City> = mutableListOf()

    /** Активные санкции против этой страны */
    val incomingSanctions: MutableSet<Country> = mutableSetOf()

    init {
        addCitizen(president)
        president.role = WDPlayerRole.PRESIDENT

        val capitalizations = distributeCapitalization()
        type.citiesName.forEachIndexed { index, name ->
            cities[index] = City(name, this, capitalizations[index])
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
        incomingSanctions.clear()
    }

    /** Сбор прибыли за раунд с учётом штрафа от санкций (-5% за каждого санкционера, макс 45%) */
    fun collectRoundProfit() {
        if (game.round == 0) return
        val penalty = (SANCTION_INCOME_PENALTY * incomingSanctions.size).coerceAtMost(MAX_SANCTION_PENALTY)
        balance += (levelOfLife * 13 * (1.0 - penalty)).toInt()
    }

    /** @return true если ядерная технология успешно изучена */
    fun learnNuclear(): Boolean {
        if (isNuclearLearned) return false
        if (balance < LEARN_NUCLEAR_COST) return false
        balance -= LEARN_NUCLEAR_COST
        isNuclearLearned = true
        game.ecology -= ECOLOGY_LEARN_PENALTY
        game.history.record(WDHistoryEntry(WDAction.NUCLEAR_LEARNED, game.round, actor = this))
        return true
    }

    /** @return true если ядерная бомба успешно создана */
    fun createNuclearBomb(): Boolean {
        if (balance < CRAFT_NUCLEAR_BOMB_COST) return false
        balance -= CRAFT_NUCLEAR_BOMB_COST
        bombsMaking += 1
        game.ecology -= ECOLOGY_CRAFT_PENALTY
        game.history.record(WDHistoryEntry(WDAction.NUCLEAR_BOMB_CREATED, game.round, actor = this))
        return true
    }

    /** @return true если вклад в экологию успешно сделан */
    fun investmentsEcology(): Boolean {
        if (balance < ECOLOGY_COST) return false
        balance -= ECOLOGY_COST
        game.ecology += ECOLOGY_INVEST_GAIN
        game.history.record(WDHistoryEntry(WDAction.ECOLOGY_INVESTED, game.round, actor = this))
        return true
    }

    /**
     * Ставит бомбардировку города в очередь.
     * @return true если бомбардировка успешно поставлена в очередь
     */
    fun scheduleBombardment(city: City): Boolean {
        if (city in this.cities.values) return false
        if (bombsAvailable <= 0) return false
        bombsAvailable -= 1
        pendingBombardments += city
        game.history.record(WDHistoryEntry(WDAction.BOMBARDMENT, game.round, actor = this, targetCountry = city.country, targetCity = city))
        return true
    }

    /** Применяет все отложенные бомбардировки */
    fun resolvePendingBombardments() {
        pendingBombardments.forEach { it.bombardCity() }
        pendingBombardments.clear()
    }

    /** Накладывает санкции на 1 раунд */
    fun sanctionCountry(target: Country): Boolean {
        if (this == target) return false
        target.incomingSanctions.add(this)
        game.history.record(WDHistoryEntry(WDAction.SANCTION, game.round, actor = this, targetCountry = target))
        return true
    }

    /** @return действия страны */
    fun spy(target: Country): Map<WDAction, Int>? {
        if (this == target) return null
        if (balance < SPY_COST) return null
        balance -= SPY_COST
        game.history.record(WDHistoryEntry(WDAction.SPY, game.round, actor = this, targetCountry = target))
        return game.history.getActionsOf(target, game.round)
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
        const val SPY_COST: Int = 75

        const val ECOLOGY_INVEST_GAIN: Double = 1.0 / 60
        const val ECOLOGY_CRAFT_PENALTY: Double = 1.0 / 180
        const val ECOLOGY_LEARN_PENALTY: Double = 0.05
        const val SANCTION_INCOME_PENALTY: Double = 0.05
        const val MAX_SANCTION_PENALTY: Double = 0.45

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

        /**
         * Распределяет [total] очков между [count] городами так, что
         * каждое значение находится в диапазоне [base ± deviation] и сумма == total.
         */
        private fun distributeCapitalization(
            total: Int = 300,
            count: Int = 4,
            deviation: Int = 20
        ): IntArray {
            val base = total / count
            val rng = ThreadLocalRandom.current()
            val deltas = IntArray(count)
            var accumulated = 0

            for (i in 0 until count - 1) {
                val remaining = count - 1 - i
                val lo = maxOf(-deviation, -accumulated - deviation * remaining)
                val hi = minOf(deviation, -accumulated + deviation * remaining)
                deltas[i] = rng.nextInt(lo, hi + 1)
                accumulated += deltas[i]
            }
            deltas[count - 1] = -accumulated

            return IntArray(count) { base + deltas[it] }
        }
    }
}