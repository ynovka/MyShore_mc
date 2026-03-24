package ru.ynovka.myShore.games.tag

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.GamePlayer

class TagPlayer(
    player: Player,
    var role: TagPlayerRoles = TagPlayerRoles.UNDEFINED,
) : GamePlayer(player)
