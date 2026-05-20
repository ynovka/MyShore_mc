package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.utils.InstantChunkClear
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.game.GameWorldOld
import org.bukkit.Location
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.UUID


class PillarsWorldOld(
    val worldId: UUID
) : GameWorldOld {

    override val world: World
        get() = Bukkit.getWorld(worldId) ?: throw IllegalStateException("World with id $worldId is not loaded")

    val pillars: MutableSet<Pillar> = mutableSetOf()

    fun countdownPrepare() {
        val chunks = world.loadedChunks.map { it.x to it.z }

        chunks.forEach { (chunkX, chunkZ) ->
            scheduler.schedule {
                val chunk = world.getChunkAt(chunkX, chunkZ)

                if (!Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
                    inst.logger.warning(
                        "[InstantChunkClear] Chunk $chunkX,$chunkZ is not owned by current region"
                    )
                    return@schedule
                }

                InstantChunkClear.clearChunk(chunk)
            }
                .region(Location(world, chunkX * 16.0 + 8.0, 0.0, chunkZ * 16.0 + 8.0))
                .once()
        }
    }
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
