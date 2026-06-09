package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.game.pillars.generators.platform.PlatformGenerator
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.game.pillars.states.PillarsWaitingForPlayers
import ru.ynovka.myShore.game.pillars.states.PillarsCountdown
import ru.ynovka.myShore.game.pillars.gameMode.PillarsGameMode
import ru.ynovka.myShore.party.PartyManager.Party
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.Game
import org.bukkit.entity.Player
import java.util.UUID


class PillarsGame(party: Party? = null) : Game<PillarsPlayer, PillarsWorld>(party) {

    override fun createPlayer(playerId: UUID) = PillarsPlayer(playerId)
    override val initialState = PillarsWaitingForPlayers(this)
    override val maxPlayers: Int = 250

    var nextRoundGameMode = PillarsGameMode.NULL
    var nextRoundAllocator = AllocatorGenerator.HONEY
    var nextRoundPlatform = PlatformGenerator.NULL
    var nextRoundPillar = PillarGenerator.DEFAULT

    var roundGameMode = nextRoundGameMode

    override val gameWorld = PillarsWorld(
        nextRoundPillar,
        nextRoundAllocator,
        nextRoundPlatform
    )

    fun applyNextRoundGenerators() {
        gameWorld.pillarGen = nextRoundPillar
        gameWorld.allocatorGen = nextRoundAllocator
        gameWorld.platformGen = nextRoundPlatform
        roundGameMode = nextRoundGameMode
    }

    fun startNextRound() {
        if (activePlayers.size >= 2) {
            fsm.transitionTo(PillarsCountdown(this))
        } else {
            fsm.transitionTo(PillarsWaitingForPlayers(this))
        }
    }

    fun canOwnerControl(player: Player): Boolean {
        val party = party ?: return false
        return party.owner == player.uniqueId && player.uniqueId in party.members
    }

    companion object {
        fun UUID.currentPillarsGame(): PillarsGame? = GameManager.run { currentGame() }
    }
}
