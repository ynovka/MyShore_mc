package ru.ynovka.myShore.games

import ru.ynovka.myShore.party.PartyManager.Party
import java.util.concurrent.CopyOnWriteArrayList
import ru.ynovka.myShore.party.PartyManager
import org.bukkit.entity.Player
import ru.ynovka.myShore.utils.Utils.asPlayers

object GameManager {
    private val games: MutableList<Game<*>> = CopyOnWriteArrayList()

    fun Player.currentGame(): Game<*>? = games.firstOrNull { it.hasPlayer(uniqueId) }
    fun Player.inGame(): Boolean = currentGame() != null

    fun <G : Game<*>> join(
        player: Player,
        factory: () -> G,
        partyFactory: (Party) -> G = { factory() },
    ): Result<G> {
        if (player.inGame())
            return Result.failure(IllegalStateException("Player ${player.name} is already in a game"))

        player.inventory.clear()
        player.activePotionEffects.clear()

        val party = PartyManager.getParty(player)
        return if (party != null)
            joinPrivate(party, factory, partyFactory)
        else
            joinPublic(player, factory)
    }

    private fun <G : Game<*>> joinPublic(player: Player, factory: () -> G): Result<G> {
        val templateClass = factory().javaClass
        @Suppress("UNCHECKED_CAST")
        val existing = games.firstOrNull { g ->
            g.javaClass == templateClass && !g.isPrivate && !g.isFull()
        } as? G

        val game = existing ?: factory().also { newGame ->
            games.add(newGame)
        }

        game.onPlayerJoin(player)
        return Result.success(game)
    }

    private fun <G : Game<*>> joinPrivate(
        party: Party,
        factory: () -> G,
        partyFactory: (Party) -> G,
    ): Result<G> {
        val templateClass = factory().javaClass
        @Suppress("UNCHECKED_CAST")
        val existing = games.firstOrNull { g ->
            g.javaClass == templateClass && g.party == party
        } as? G

        val game = existing ?: partyFactory(party).also { newGame ->
            games.add(newGame)
        }

        party.members.asPlayers()
            .filter { !it.inGame() }
            .forEach { member -> game.onPlayerJoin(member) }

        return Result.success(game)
    }

    fun leave(player: Player) {
        val game = player.currentGame() ?: return
        game.onPlayerLeave(player)
        if (game.isEmpty()) games.remove(game)
    }
}