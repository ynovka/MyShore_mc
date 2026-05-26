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
    override val maxPlayers: Int = 500
    override fun createPlayer(playerId: UUID) = PillarsPlayer(playerId)

    var nextRoundPillar = PillarGenerator.DEFAULT
    var nextRoundAllocator = AllocatorGenerator.HONEY
    override val gameWorld = PillarsWorld(nextRoundPillar, nextRoundAllocator)

    companion object {
        val hubWorld = HubGameWorld("pillars")
        fun UUID.currentPillarsGame(): PillarsGame? = GameManager.run { currentGame() }
    }
}