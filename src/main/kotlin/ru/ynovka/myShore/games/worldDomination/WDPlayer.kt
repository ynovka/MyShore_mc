package ru.ynovka.myShore.games.worldDomination

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.GamePlayer

class WDPlayer(
    player: Player,
    val role: PlayerRole = PlayerRole.UNDEFINED
) : GamePlayer(player)

enum class PlayerRole {
    UNDEFINED,
    PRESIDENT,
    VICE_PRESIDENT,
    CITIZEN,
    SPECTATOR
}