package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.worldDomination.entity.CountryType
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GamePlayer
import ru.ynovka.myShore.games.worldDomination.WDItems
import ru.ynovka.myShore.games.worldDomination.WDPlayerRole

/**
 * Этап распределния игроков по странам, длится 3 минуты
 * Снача определяется кол-во стран (кол-во игроков / 2, max 10)
 * Для тестов - минимум 2 игрока (2с по 1и)
 * Минимум 12 игроков (6с по 2и)
 * Максимум 50 игроков (10с по 5и)
 */
object WDDistributionPlayers : GameState<WDPlayer> {
    /**
     * Случайным образом определяем президентов случайных стран и телепортируем их в штаб-квартиры
     * Остальные игроки остаются в лобби
     * У президентов есть 3 минуты что бы выбрать своего вице-президента
     * По истечению 3-ёх минут странам без вице-президентов они будут назначенны случайным образом
     */
    override fun onEnter(game: Game<WDPlayer>) {
        // Случайное распределение стран и президентов
        val players = game.gamePlayers.shuffled()
        val countriesCount = (players.size / 2).coerceIn(2..10)
        val countries = CountryType.entries.shuffled().take(countriesCount)

        players.map(GamePlayer::player).forEach { it.inventory.setItem(8, WDItems.wdNotebook.getStack(it)) }

        players.take(countriesCount).zip(countries)
            .forEach { (president, type) ->
                val pp = president.player
                // Создаём страну, телепортируем в неё презиента
                Country.create(president, type).also { country ->
                    country.teleport(pp)
                }

                // Выдаём президенту телефон для звонков
                pp.inventory.setItem(0, WDItems.wdPhoneMenu.getStack(pp))
            }

        // Отсчёт 3 минуты, до перехода к следующему этапу
        inst.server.scheduler.runTaskLater(inst, Runnable {
            game.fsm.transitionTo(WDDistributionPlayers)
        }, 3 * 60 * 20L)
    }

    override fun onExit(game: Game<WDPlayer>) {
        game.gamePlayers
            .filter { it.role == WDPlayerRole.PRESIDENT }
            .map(GamePlayer::player)
            .forEach { it.inventory.clear(0) }
    }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerReconnect(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerLeave(game: Game<WDPlayer>, player: WDPlayer) { }
}