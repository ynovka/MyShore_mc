package ru.ynovka.myShore.games.worldDomination

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.GamePlayer
import ru.ynovka.myShore.games.worldDomination.WDGame.Companion.currentWDGame
import java.util.UUID


class WDPlayer(
    playerId: UUID,
    val role: WDPlayerRole = WDPlayerRole.UNDEFINED
) : GamePlayer(playerId) {
    var country: Country? = null

    companion object {
        fun Player.asWDPlayer(): WDPlayer? = currentWDGame()?.getOrCreatePlayer(this)
    }
}

enum class WDPlayerRole {
    UNDEFINED,
    PRESIDENT,
    VICE_PRESIDENT,
    CITIZEN,
    SPECTATOR
}