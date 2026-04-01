package ru.ynovka.myShore.games.worldDomination

import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.GamePlayer
import java.util.UUID


class WDPlayer(
    playerId: UUID,
    val role: WDPlayerRole = WDPlayerRole.UNDEFINED
) : GamePlayer(playerId) {
    var country: Country? = null
}

enum class WDPlayerRole {
    UNDEFINED,
    PRESIDENT,
    VICE_PRESIDENT,
    CITIZEN,
    SPECTATOR
}