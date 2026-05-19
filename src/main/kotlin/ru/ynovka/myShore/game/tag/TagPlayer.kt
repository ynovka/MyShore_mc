package ru.ynovka.myShore.game.tag

import ru.ynovka.myShore.game.GamePlayer
import java.util.UUID

class TagPlayer(
    playerId: UUID,
    var role: TagPlayerRoles = TagPlayerRoles.UNDEFINED,
) : GamePlayer(playerId)
