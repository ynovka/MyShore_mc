package ru.ynovka.myShore.utils

import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import org.bukkit.Location
import java.util.Random
import org.bukkit.World


class VoidWorldGenerator : ChunkGenerator() {

    override fun generateNoise(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) { }

    override fun generateSurface(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) { }

    override fun generateBedrock(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) { }

    override fun generateCaves(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) { }

    override fun getBaseHeight(
        worldInfo: WorldInfo,
        random: Random,
        x: Int,
        z: Int,
        heightMap: org.bukkit.HeightMap
    ): Int = worldInfo.minHeight

    override fun canSpawn(world: World, x: Int, z: Int): Boolean = true

    override fun getFixedSpawnLocation(world: World, random: Random): Location {
        return Location(world, 0.5, 100.0, 0.5)
    }

    override fun shouldGenerateNoise(): Boolean = false
    override fun shouldGenerateSurface(): Boolean = false
    override fun shouldGenerateCaves(): Boolean = false
    override fun shouldGenerateDecorations(): Boolean = false
    override fun shouldGenerateMobs(): Boolean = false
    override fun shouldGenerateStructures(): Boolean = false
}