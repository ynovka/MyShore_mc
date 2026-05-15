package ru.ynovka.myShore.utils

import com.github.darksoulq.abyssallib.common.reflection.Reflect
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.ticks.LevelChunkTicks
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.craftbukkit.CraftChunk
import net.minecraft.core.SectionPos
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.Chunk


object InstantChunkClear {

    fun clearChunk(chunk: Chunk) {
        check(Bukkit.isPrimaryThread()) { "clearChunk must be called only on the main server thread" }

        val nmsChunk = (chunk as CraftChunk).getHandle(ChunkStatus.FULL) as LevelChunk

        chunk.entities.forEach { if (it !is Player) it.remove() }

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
        for (type in Heightmap.Types.entries) {
            if (!nmsChunk.heightmaps.containsKey(type)) {
                nmsChunk.setHeightmap(type, LongArray(256))
            }
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
            .field<Array<Any?>>("gameEventListenerRegistrySections")
            .ifSuccess { it.get(chunk).ifSuccess { arr -> arr.fill(null) } }
            .ifFailure {
                inst.logger.warning("[InstantChunkClear] gameEventListenerRegistrySections: ${it.message}")
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