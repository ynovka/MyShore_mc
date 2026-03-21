package ru.ynovka.myShore.games.worldDomination.states

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame


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
object WDUNMeetingState : GameState<WDGame> {
    override fun onStateStart(game: WDGame) {}

    override fun onPlayerJoin(game: WDGame, player: Player) {}
}