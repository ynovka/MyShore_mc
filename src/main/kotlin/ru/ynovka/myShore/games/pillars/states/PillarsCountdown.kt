package ru.ynovka.myShore.games.pillars.states

import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.pillars.PillarsGame
import ru.ynovka.myShore.games.pillars.PillarsPlayer
import ru.ynovka.myShore.games.pillars.PillarsWorld


/**
 * Стадия обратного отсчёта 10 секунд, парралельно голосование за режим игры
 * Голосование - меню с 3 разделами:
 *  - точки спавна (кольцо, медовые соты)
 *  - игровая карта (определяет пол, и декор столбов):
 *   - стандартная (столбы из бедрока, 64 блока высотой, без платформы)
 *   - бездна (стобы из бедрока, 4 блока высотой, без платформы)
 *   - паутина (сандартная + основание из булыжника с паутиной + пауки)
 *     // Примечание, что мобы спавнятся разово в начале раунда, без спавнеров и в дневное время суток
 *   - батут (сандартная + основание из шума блоков слизи и изумрудов + слизни)
 */
class PillarsCountdown(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {
    override fun onEnterState() {
        game.gamePlayers += game.spectatorPlayers
        game.spectatorPlayers.clear()
    }
}