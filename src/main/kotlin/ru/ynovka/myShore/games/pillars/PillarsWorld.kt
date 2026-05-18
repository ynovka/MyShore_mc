package ru.ynovka.myShore.games.pillars

import ru.ynovka.myShore.utils.InstantChunkClear
import ru.ynovka.myShore.games.GameWorld
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.UUID


class PillarsWorld(
    val worldId: UUID
) : GameWorld {

    override val world: World
        get() = Bukkit.getWorld(worldId) ?: throw IllegalStateException("World with id $worldId is not loaded")

    val pillars: MutableSet<Pillar> = mutableSetOf()

    fun countdownPrepare() = world.loadedChunks.forEach { InstantChunkClear.clearChunk(it) }
}

data class Pillar(
    val x: Int,
    val z: Int,
    val owner: UUID,
    val footprint: Footprint = Footprints.single
) {
    companion object {
        const val TOP_BLOCK = 100
        const val TELEPORT_Y = 110.0
    }
}
