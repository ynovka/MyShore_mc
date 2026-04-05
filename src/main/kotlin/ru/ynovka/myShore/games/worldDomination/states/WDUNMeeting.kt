package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDPlayer


/**
 * Этап совещания ООН
 * Длится от 6 до 10 минут (кол-во стран * 1 минута)
 * (возможно нужно дать 10 секунд на подтверждения выступления, если не подтвердить выступление -
 * страну переместит в конец выступления, работает 1 раз за совещание, иначе речь пропускается)
 * В этот период каждой стране даётся 1 минута на любую речь
 * Порядок стран для выступления:
 * - прилетело больше бомб
 * - уровень развития
 * - название по алфавиту
 */
class WDUNMeeting(game: Game<WDPlayer>) : GameState<WDPlayer>(game) {
    override fun onEnter() { }

    override fun onExit() { }

    override fun onPlayerJoin(player: WDPlayer) { }

    override fun onPlayerReconnect(player: WDPlayer) { }

    override fun onPlayerLeave(player: WDPlayer) { }

    override fun canPlayerJoin(player: WDPlayer): Boolean = false
}