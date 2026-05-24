package ru.ynovka.myShore.utils

import net.minecraft.world.level.gameevent.GameEventListenerRegistry
import com.github.darksoulq.abyssallib.common.reflection.Reflect
import net.minecraft.world.level.chunk.status.ChunkStatus
import ru.ynovka.myShore.MyShore.Companion.scheduler
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.ticks.LevelChunkTicks
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.craftbukkit.CraftChunk
import net.minecraft.core.SectionPos
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.Chunk


object InstantChunkClear {

    fun clearChunk(chunk: Chunk) {
        check(Bukkit.isOwnedByCurrentRegion(chunk.world, chunk.x, chunk.z)) {
            "clearChunk must be called only on the owning region thread"
        }

        val nmsChunk = (chunk as CraftChunk).getHandle(ChunkStatus.FULL) as LevelChunk

        chunk.entities.filter { it !is Player }.forEach {
            scheduler.schedule {
                it.remove()
            }.entity(it).once()
        }

        nmsChunk.clearAllBlockEntities()
        clearPendingBlockEntities(nmsChunk)
        clearGameEventRegistries(nmsChunk)
        clearTicks(nmsChunk)

        val emptyChunk = LevelChunk(nmsChunk.level, nmsChunk.pos)
        val lightEngine = nmsChunk.level.chunkSource.lightEngine

        for (i in nmsChunk.sections.indices) {
            nmsChunk.sections[i] = emptyChunk.sections[i]
            val sectionY = nmsChunk.getSectionYFromSectionIndex(i)
            lightEngine.updateSectionStatus(SectionPos.of(chunk.x, sectionY, chunk.z), true)
            nmsChunk.level.chunkSource.onSectionEmptinessChanged(chunk.x, sectionY, chunk.z, true)
        }

        for ((type, heightmap) in emptyChunk.heightmaps) {
            nmsChunk.setHeightmap(type, heightmap.rawData)
        }

        Reflect.of(LevelChunk::class.java)
            .method<Void>("initializeLightSources")
            .ifSuccess { it.invoke(nmsChunk) }

        nmsChunk.markUnsaved()
        chunk.world.refreshChunk(chunk.x, chunk.z)
    }

    private fun clearPendingBlockEntities(chunk: LevelChunk) {
        Reflect.of(ChunkAccess::class.java)
            .field<MutableMap<*, *>>("pendingBlockEntities")
            .ifSuccess { it.get(chunk).ifSuccess { map -> map.clear() } }
    }

    private fun clearGameEventRegistries(chunk: LevelChunk) {
        Reflect.of(LevelChunk::class.java)
            .field<Int2ObjectMap<GameEventListenerRegistry>>("gameEventListenerRegistrySections")
            .ifSuccess { field ->
                field.get(chunk)
                    .ifSuccess { registries -> registries.clear() }
                    .ifFailure {
                        inst.logger.warning("[InstantChunkClear] gameEventListenerRegistrySections get: ${it.message}")
                    }
            }
            .ifFailure {
                inst.logger.warning("[InstantChunkClear] gameEventListenerRegistrySections field: ${it.message}")
            }
    }

    private fun clearTicks(chunk: LevelChunk) {
        Reflect.of(LevelChunk::class.java)
            .field<LevelChunkTicks<*>>("blockTicks")
            .ifSuccess { field ->
                field.get(chunk).ifSuccess { ticks ->
                    Reflect.of(LevelChunkTicks::class.java)
                        .method<Void>("clear")
                        .ifSuccess { it.invoke(ticks) }
                }
            }
        Reflect.of(LevelChunk::class.java)
            .field<LevelChunkTicks<*>>("fluidTicks")
            .ifSuccess { field ->
                field.get(chunk).ifSuccess { ticks ->
                    Reflect.of(LevelChunkTicks::class.java)
                        .method<Void>("clear")
                        .ifSuccess { it.invoke(ticks) }
                }
            }
    }
}