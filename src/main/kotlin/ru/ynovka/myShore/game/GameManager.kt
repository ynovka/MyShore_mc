package ru.ynovka.myShore.game

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.party.PartyManager.Party
import java.util.concurrent.CopyOnWriteArrayList
import ru.ynovka.myShore.party.PartyManager
import org.bukkit.entity.Player
import kotlin.reflect.KClass
import java.util.UUID


object GameManager {
    val games: MutableList<Game<*, *>> = CopyOnWriteArrayList()

    inline fun <reified G : Game<*, *>> UUID.currentGame(): G? =
        games.firstOrNull { it is G && it.hasPlayer(this) } as? G

    fun UUID.inGame(): Boolean = currentGame<Game<*, *>>() != null

    inline fun <reified G : Game<*, *>> join(
        player: Player,
        noinline factory: () -> G,
        noinline partyFactory: (Party) -> G = { factory() },
    ): Result<G> {
        if (player.uniqueId.inGame())
            return Result.failure(IllegalStateException("Player ${player.name} is already in a game"))

        scheduler.schedule {
            player.inventory.clear()
            player.clearActivePotionEffects()
        }.entity(player).once()

        val party = PartyManager.getParty(player)
        return if (party != null)
            joinPrivate<G>(party, partyFactory)
        else
            joinPublic<G>(player.uniqueId, factory)
    }

    @PublishedApi
    internal inline fun <reified G : Game<*, *>> joinPublic(
        playerId: UUID,
        noinline factory: () -> G
    ): Result<G> {
        @Suppress("UNCHECKED_CAST")
        val existing = games.firstOrNull { g ->
            g is G && !g.isPrivate && !g.isFull()
        } as? G

        val game = existing ?: factory().also { newGame ->
            games.add(newGame)
        }

        game.onPlayerJoin(playerId)
        return Result.success(game)
    }

    @PublishedApi
    internal inline fun <reified G : Game<*, *>> joinPrivate(
        party: Party,
        noinline partyFactory: (Party) -> G,
    ): Result<G> {
        @Suppress("UNCHECKED_CAST")
        val existing = games.firstOrNull { g ->
            g is G && g.party == party
        } as? G

        val game = existing ?: partyFactory(party).also { newGame ->
            games.add(newGame)
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
        if (games.remove(game)) {
            game.destroy()
        }
    }
}