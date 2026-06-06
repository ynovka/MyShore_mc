package ru.ynovka.myShore.game.pillars.generators.platform

import java.util.concurrent.CompletableFuture
import ru.ynovka.myShore.game.pillars.Pillar
import kotlin.math.ceil
import org.bukkit.World
import kotlin.math.abs
import kotlin.math.floor


interface PlatformGen {
    fun generate(world: World, pillars: Collection<Pillar>): CompletableFuture<Void>
}

enum class PlatformGenerator(
    val gen: PlatformGen
) {
    NULL(NullPlatformGen),
    GRASS_BLOCKS(GrassBlocksPlatformGen),
    SLIME_AND_EMERALD_BLOCKS(SlimeAndEmeraldBlocksPlatformGen),
}

const val PLATFORM_Y = Pillar.TOP_BLOCK - 65
const val SQRT_THREE = 1.7320508075688772
const val HEX_SIDE_PADDING = 13

fun platformSideLength(pillars: Collection<Pillar>): Int {
    val requiredSideLength = pillars.maxOf { pillar ->
        maxOf(
            abs(pillar.x) + abs(pillar.z) / SQRT_THREE,
            abs(pillar.z) * 2.0 / SQRT_THREE
        )
    }

    return ceil(requiredSideLength).toInt() + HEX_SIDE_PADDING
}

fun collectHexagonBlocks(
    sideLength: Int,
    blocksByChunk: MutableMap<ChunkPoint, MutableSet<BlockPoint>>
) {
    val halfHeight = floor(sideLength * SQRT_THREE / 2.0).toInt()

    for (z in -halfHeight..halfHeight) {
        val halfWidth = floor(sideLength - abs(z) / SQRT_THREE).toInt()

        for (x in -halfWidth..halfWidth) {
            val block = BlockPoint(x, z)
            val chunk = ChunkPoint(
                x = Math.floorDiv(block.x, 16),
                z = Math.floorDiv(block.z, 16)
            )

            blocksByChunk.getOrPut(chunk) { HashSet() } += block
        }
    }
}

data class BlockPoint(
    val x: Int,
    val z: Int
)

data class ChunkPoint(
    val x: Int,
    val z: Int
)