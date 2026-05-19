package ru.ynovka.myShore.game.worldDomination

import org.bukkit.entity.Player
import ru.ynovka.myShore.game.worldDomination.entity.Country
import ru.ynovka.myShore.game.GamePlayer
import ru.ynovka.myShore.game.worldDomination.WDGame.Companion.currentWDGame
import java.util.UUID


class WDPlayer(
    playerId: UUID,
    var role: WDPlayerRole = WDPlayerRole.UNDEFINED
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