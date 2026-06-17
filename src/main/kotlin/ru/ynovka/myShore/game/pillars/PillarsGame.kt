package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.game.pillars.generators.platform.PlatformGenerator
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.game.pillars.states.PillarsWaitingForPlayers
import ru.ynovka.myShore.game.pillars.states.PillarsCountdown
import ru.ynovka.myShore.game.pillars.states.PillarsFinishing
import ru.ynovka.myShore.game.pillars.states.PillarsInProgress
import ru.ynovka.myShore.game.pillars.gameMode.PillarsGameMode
import ru.ynovka.myShore.party.PartyManager.Party
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.Game
import org.bukkit.entity.Player
import java.util.UUID


class PillarsGame(party: Party? = null) : Game<PillarsPlayer, PillarsWorld>(party) {
    private val roundConfig = PillarsRoundConfig()

    override fun createPlayer(playerId: UUID) = PillarsPlayer(playerId)
    override val initialState = PillarsWaitingForPlayers(this)
    override val maxPlayers: Int = 250

    var nextRoundGameMode: PillarsGameMode
        get() = roundConfig.next.gameMode
        set(value) {
            roundConfig.next = roundConfig.next.copy(gameMode = value)
        }

    var nextRoundAllocator: AllocatorGenerator
        get() = roundConfig.next.allocator
        set(value) {
            roundConfig.next = roundConfig.next.copy(allocator = value)
        }

    var nextRoundPlatform: PlatformGenerator
        get() = roundConfig.next.platform
        set(value) {
            roundConfig.next = roundConfig.next.copy(platform = value)
        }

    var nextRoundPillar: PillarGenerator
        get() = roundConfig.next.pillar
        set(value) {
            roundConfig.next = roundConfig.next.copy(pillar = value)
        }

    val roundGameMode: PillarsGameMode
        get() = roundConfig.current.gameMode

    override val gameWorld = PillarsWorld(
        roundConfig.next.pillar,
        roundConfig.next.allocator,
        roundConfig.next.platform
    )

    fun applyNextRoundGenerators() =
        roundConfig.applyNextTo(gameWorld)

    fun startNextRound() =
        transitionToRoundStart()

    fun transitionToRoundStart() {
        if (activePlayers.size >= 2) {
            transitionToCountdown()
        } else {
            transitionToWaitingForPlayers()
        }
    }

    fun transitionToWaitingForPlayers() =
        transitionTo(PillarsWaitingForPlayers(this))

    fun transitionToCountdown() =
        transitionTo(PillarsCountdown(this))

    fun transitionToInProgress() =
        transitionTo(PillarsInProgress(this))

    fun transitionToFinishing() =
        transitionTo(PillarsFinishing(this))

    fun isInProgress(): Boolean =
        isInState<PillarsInProgress>()

    fun isFinishing(): Boolean =
        isInState<PillarsFinishing>()

    fun canOwnerControl(player: Player): Boolean {
        val party = party ?: return false
        return party.owner == player.uniqueId && player.uniqueId in party.members
    }

    companion object {
        fun UUID.currentPillarsGame(): PillarsGame? = GameManager.run { currentGame() }
    }
}
