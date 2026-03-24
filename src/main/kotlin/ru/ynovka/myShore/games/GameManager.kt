package ru.ynovka.myShore.games

import ru.ynovka.myShore.party.PartyManager.Party
import java.util.concurrent.CopyOnWriteArrayList
import ru.ynovka.myShore.party.PartyManager
import org.bukkit.entity.Player
import kotlin.reflect.KClass


object GameManager {
    private val games: MutableList<Game<*>> = CopyOnWriteArrayList()

    fun <G : Game<*>> getGames(type: KClass<G>): List<G> = games.filterIsInstance(type.java)

    fun Player.currentGame(): Game<*>? = games.firstOrNull { it.hasPlayer(uniqueId) }

    fun Player.inGame(): Boolean = currentGame() != null

    private fun <G : Game<*>> KClass<G>.createInstance(party: Party? = null): G =
        java.getDeclaredConstructor(Party::class.java).newInstance(party)

    fun <G : Game<*>> join(player: Player, type: KClass<G>): Result<G> {
        if (player.inGame()) return Result.failure(IllegalStateException("Player ${player.name} is already in a game"))

        val party = PartyManager.getParty(player)
        return if (party != null) joinPrivate(player, party, type)

        else joinPublic(player, type)
    }

    private fun <G : Game<*>> joinPublic(player: Player, type: KClass<G>): Result<G> {
        val game = games.filterIsInstance(type.java).firstOrNull { !it.isPrivate && !it.isFull() }
            ?: type.createInstance().also { games.add(it) }
        game.onPlayerJoin(player)
        return Result.success(game)
    }

    private fun <G : Game<*>> joinPrivate(player: Player, party: Party, type: KClass<G>): Result<G> {
        val game = games.filterIsInstance(type.java).firstOrNull { it.party == party }
            ?: type.createInstance(party).also { games.add(it) }
        game.onPlayerJoin(player)
        return Result.success(game)
    }

    fun reconnect(player: Player) {
        val game = player.currentGame() ?: return
        game.onPlayerReconnect(player)
    }

    fun leave(player: Player) {
        val game = player.currentGame() ?: return
        game.onPlayerLeave(player)
        if (game.isEmpty()) games.remove(game)
    }
}