package ru.ynovka.myShore.games.pillars

import ru.ynovka.myShore.utils.InstantChunkClear
import ru.ynovka.myShore.games.GameWorld
import java.util.UUID


class PillarsWorld(
    worldId: UUID
) : GameWorld(worldId) {

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