package ru.ynovka.myShore.utils

import org.bukkit.Location
import org.bukkit.Bukkit


data class MapSpawn(
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f
) {
    fun toLocation(): Location {
        val world = Bukkit.getWorld(worldName)
            ?: error("World '$worldName' is not loaded")
        return Location(world, x, y, z, yaw, pitch)
    }
}
