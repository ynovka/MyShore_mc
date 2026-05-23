package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.game.pillars.states.PillarsWaitingForPlayers
import ru.ynovka.myShore.game.HubGameWorld
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.Game
import java.util.UUID


class PillarsGame : Game<PillarsPlayer, PillarsWorld>() {

    override val initialState = PillarsWaitingForPlayers(this)
    override val maxPlayers: Int = 50
    override val gamePlayers: MutableSet<PillarsPlayer> = mutableSetOf()

    var pillar = PillarGenerator.DEFAULT
    var allocator = AllocatorGenerator.HONEY
    var nextRoundPillar = pillar
    var nextRoundAllocator = allocator
    override val gameWorld = PillarsWorld()

    override fun getOrCreatePlayer(playerId: UUID): PillarsPlayer =
        gamePlayers.firstOrNull { it.player.uniqueId == playerId }
            ?: PillarsPlayer(playerId)

    companion object {
        val hubWorld = HubGameWorld("pillars")
        fun UUID.currentPillarsGame(): PillarsGame? = GameManager.run { currentGame() }
    }
}