package ru.ynovka.myShore.game

import ru.ynovka.myShore.party.PartyManager.Party
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList


class GameRegistry {
    val games: MutableList<Game<*, *>> = CopyOnWriteArrayList()

    fun hasPlayer(playerId: UUID): Boolean =
        games.any { it.hasPlayer(playerId) }

    inline fun <reified G : Game<*, *>> currentGame(playerId: UUID): G? {
        @Suppress("UNCHECKED_CAST")
        return games.firstOrNull { it is G && it.hasPlayer(playerId) } as? G
    }

    inline fun <reified G : Game<*, *>> publicReconnectGame(playerId: UUID): G? {
        @Suppress("UNCHECKED_CAST")
        return games.firstOrNull { game ->
            game is G && !game.isPrivate && game.hasExitedPlayer(playerId)
        } as? G
    }

    inline fun <reified G : Game<*, *>> publicAvailableGame(playerId: UUID): G? {
        @Suppress("UNCHECKED_CAST")
        return games.firstOrNull { game ->
            game is G && !game.isPrivate && game.canAcceptNewPlayer(playerId)
        } as? G
    }

    inline fun <reified G : Game<*, *>> privateGame(party: Party): G? {
        @Suppress("UNCHECKED_CAST")
        return games.firstOrNull { game ->
            game is G && game.party == party
        } as? G
    }

    fun add(game: Game<*, *>) {
        games.add(game)
    }

    fun remove(game: Game<*, *>): Boolean =
        games.remove(game)
}
