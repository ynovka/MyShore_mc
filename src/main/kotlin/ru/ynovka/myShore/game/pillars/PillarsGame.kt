package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.game.pillars.generators.platform.PlatformGenerator
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.game.pillars.states.PillarsWaitingForPlayers
import ru.ynovka.myShore.game.pillars.gameMode.PillarsGameMode
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.Game
import java.util.UUID


class PillarsGame : Game<PillarsPlayer, PillarsWorld>() {

    override fun createPlayer(playerId: UUID) = PillarsPlayer(playerId)
    override val initialState = PillarsWaitingForPlayers(this)
    override val maxPlayers: Int = 250

    var nextRoundGameMode = PillarsGameMode.HOTBAR_ITEMS
    var nextRoundAllocator = AllocatorGenerator.HONEY
    var nextRoundPlatform = PlatformGenerator.GRASS_BLOCKS
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

    companion object {
        fun UUID.currentPillarsGame(): PillarsGame? = GameManager.run { currentGame() }
    }
}
