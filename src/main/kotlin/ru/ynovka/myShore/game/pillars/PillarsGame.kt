package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.game.pillars.states.PillarsWaitingForPlayers
import ru.ynovka.myShore.text.actionBar.clearActionBar
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.game.Game
import org.bukkit.entity.Player


class PillarsGame : Game<PillarsPlayer, PillarsWorld>() {

    override val initialState = PillarsWaitingForPlayers(this)
    override val maxPlayers: Int = 50
    override val gamePlayers: MutableSet<PillarsPlayer> = mutableSetOf()

    var pillar = PillarGenerator.DEFAULT
    var allocator = AllocatorGenerator.HONEY
    var nextRoundPillar = pillar
    var nextRoundAllocator = allocator
    override val gameWorld = PillarsWorldManager.createWorld()

    override fun getOrCreatePlayer(player: Player): PillarsPlayer =
        gamePlayers.firstOrNull { it.player.uniqueId == player.uniqueId }
            ?: PillarsPlayer(player.uniqueId)

    override fun handlePlayerJoin(player: PillarsPlayer) {
        // map.onPlayerJoin(this, player.player)
    }

    override fun handlePlayerLeave(player: PillarsPlayer) {
        player.player.clearActivePotionEffects()
        player.player.canMove(true)
        player.player.clearActionBar()

        when (fsm.current) {
            else -> Unit
        }
    }
}