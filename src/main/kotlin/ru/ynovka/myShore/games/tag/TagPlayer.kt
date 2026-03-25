package ru.ynovka.myShore.games.tag

import ru.ynovka.myShore.games.GamePlayer
import java.util.UUID

class TagPlayer(
    playerId: UUID,
    var role: TagPlayerRoles = TagPlayerRoles.UNDEFINED,
) : GamePlayer(playerId)
