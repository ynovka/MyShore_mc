package ru.ynovka.myShore.game

import ru.ynovka.myShore.party.PartyManager.Party
import ru.ynovka.myShore.party.PartyManager
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import java.util.UUID


object GameManager {
    @PublishedApi
    internal val registry = GameRegistry()

    val games: MutableList<Game<*, *>>
        get() = registry.games

    inline fun <reified G : Game<*, *>> UUID.currentGame(): G? =
        registry.currentGame(this)

    fun UUID.inGame(): Boolean =
        registry.hasPlayer(this)

    inline fun <reified G : Game<*, *>> join(
        player: Player,
        noinline factory: () -> G,
        noinline partyFactory: (Party) -> G = { factory() },
    ): Result<G> {
        if (player.uniqueId.inGame())
            return Result.failure(IllegalStateException("Player ${player.name} is already in a game"))

        GamePlayerPreparation.reset(player)

        val party = PartyManager.getParty(player)
        return if (party != null)
            joinPrivate<G>(party, partyFactory)
        else
            joinPublic<G>(player.uniqueId, factory)
    }

    inline fun <reified G : Game<*, *>> joinParty(
        party: Party,
        noinline partyFactory: (Party) -> G,
    ): Result<G> {
        val joinableMembers = party.members.filter { !it.inGame() }
        if (joinableMembers.isEmpty()) {
            return Result.failure(IllegalStateException("Party has no players available to join"))
        }

        joinableMembers
            .mapNotNull(Bukkit::getPlayer)
            .let(GamePlayerPreparation::resetAll)

        return joinPrivate<G>(party, partyFactory)
    }

    @PublishedApi
    internal inline fun <reified G : Game<*, *>> joinPublic(
        playerId: UUID,
        noinline factory: () -> G
    ): Result<G> {
        val reconnectGame = registry.publicReconnectGame<G>(playerId)
        val availableGame = registry.publicAvailableGame<G>(playerId)

        val game = reconnectGame ?: availableGame ?: factory().also { newGame ->
            registry.add(newGame)
        }

        game.onPlayerJoin(playerId)
        return Result.success(game)
    }

    @PublishedApi
    internal inline fun <reified G : Game<*, *>> joinPrivate(
        party: Party,
        noinline partyFactory: (Party) -> G,
    ): Result<G> {
        val existing = registry.privateGame<G>(party)

        val game = existing ?: partyFactory(party).also { newGame ->
            registry.add(newGame)
        }

        party.members
            .filter { !it.inGame() }
            .forEach { game.onPlayerJoin(it) }

        return Result.success(game)
    }

    fun leave(playerId: UUID) {
        val game = playerId.currentGame<Game<*, *>>() ?: return

        game.onPlayerLeave(playerId)

        if (game.isEmpty()) {
            destroy(game)
        }
    }

    fun destroy(game: Game<*, *>) {
        if (registry.remove(game)) {
            game.destroy()
        }
    }
}
