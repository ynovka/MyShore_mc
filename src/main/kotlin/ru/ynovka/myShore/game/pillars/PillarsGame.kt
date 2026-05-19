package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.game.pillars.states.PillarsWaitingForPlayers
import ru.ynovka.myShore.text.actionBar.clearActionBar
import ru.ynovka.myShore.utils.canMove
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
    override val gameWorld = PillarsWorldManager.createWorld()

    override fun getOrCreatePlayer(playerId: UUID): PillarsPlayer =
        gamePlayers.firstOrNull { it.player.uniqueId == playerId }
            ?: PillarsPlayer(playerId)

    override fun handlePlayerJoin(gamePlayer: PillarsPlayer) {
        // map.onPlayerJoin(this, player.player)
    }

    override fun handlePlayerLeave(gamePlayer: PillarsPlayer) {
        gamePlayer.player.clearActivePotionEffects()
        gamePlayer.player.canMove(true)
        gamePlayer.player.clearActionBar()

        when (fsm.current) {
            else -> Unit
        }
    }
}