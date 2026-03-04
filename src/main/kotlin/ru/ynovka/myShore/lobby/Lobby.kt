package ru.ynovka.myShore.lobby

import ru.ynovka.myShore.party.PartyManager
import ru.ynovka.myShore.games.GameId
import ru.ynovka.myShore.games.Game
import org.bukkit.entity.Player
import java.util.UUID


abstract class Lobby(
    val id: Int,
    val gameType: GameId,
    val membersLimit: Int
) {
    val members: MutableSet<UUID> = hashSetOf()
    var game: Game? = null

    fun isFull() = members.size >= membersLimit
    fun isEmpty() = members.isEmpty()
    fun hasMember(uuid: UUID) = members.contains(uuid)

    fun addMember(uuid: UUID) = members.add(uuid)
    fun removeMember(uuid: UUID) = members.remove(uuid)
}

class PublicLobby(gameType: GameId) :
    Lobby(LobbyIdAllocator.acquire(), gameType, gameType.maxPlayers)

class PartyLobby(
    gameType: GameId,
    val party: PartyManager.Party
) : Lobby(LobbyIdAllocator.acquire(), gameType, gameType.maxPlayers)


fun Player.getLobby(): Lobby? = LobbyManager.lobbies.firstOrNull { it.hasMember(uniqueId) }

fun Player.hasLobby(): Boolean = getLobby() != null
