package ru.ynovka.myShore.games.worldDomination.states

import org.bukkit.Bukkit
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.worldDomination.entity.CountryType
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.Game

/**
 * Этап распределния игроков по странам
 * Снача определяется кол-во стран (кол-во игроков / 2, max 10)
 * Для тестов - минимум 2 игрока (2с по 1и)
 * Минимум 12 игроков (6с по 2и)
 * Максимум 50 игроков (10с по 5и)
 */
object WDDistributionPlayersState : GameState<WDPlayer> {
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

        players.take(countriesCount).zip(countries)
            .forEach { (president, type) ->
                // Создаём страну, телепортируем в неё презиента
                Country.create(president, type).also { country ->
                    country.teleport(president.player)
                }

                // Выдаём президенту телефон для звонков
            }

        // Отсчёт 3 минуты, до перехода к следующему этапу
        inst.server.scheduler.runTaskLater(inst, Runnable {

        }, 3 * 60 * 20L)
    }

    override fun onExit(game: Game<WDPlayer>) { }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerReconnect(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerLeave(game: Game<WDPlayer>, player: WDPlayer) { }
}