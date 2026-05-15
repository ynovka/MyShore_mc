package ru.ynovka.myShore.games.pillars

import ru.ynovka.myShore.games.pillars.PillarLoc.Companion.TELEPORT_Y
import ru.ynovka.myShore.utils.InstantChunkClear
import ru.ynovka.myShore.games.GameWorld
import org.bukkit.Location
import ru.ynovka.myShore.games.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.games.pillars.generators.pillars.PillarGenerator
import java.util.UUID


class PillarsWorld(
    worldId: UUID,
    val aGen: AllocatorGenerator,
    val pGen: PillarGenerator
) : GameWorld(worldId) {

    val pillars: MutableSet<PillarLoc> = mutableSetOf()

    fun teleport(player: PillarsPlayer, pillar: PillarLoc) {
        player.player.teleportAsync(Location(world, pillar.x + 0.5, TELEPORT_Y, pillar.z + 0.5))
    }

    fun countdownPrepare(pGame: PillarsGame) {
        // очистка чанков
        world.loadedChunks.forEach { InstantChunkClear.clearChunk(it) }

        // генерация структур колб спана игроков
        // телепорт игроков по их колбам
        pGame.gamePlayers.forEach { pPlayer ->
            val player = pPlayer.player
        }
    }
}

data class PillarLoc(val x: Int, val z: Int) {

    companion object {
        const val TOP_BLOCK = 100
        const val TELEPORT_Y = 105.0
    }
}