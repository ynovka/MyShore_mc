package ru.ynovka.myShore.games

import org.bukkit.Bukkit
import org.bukkit.World
import java.util.UUID


abstract class GameWorld(
    val worldId: UUID
) {
    val world: World
        get() = Bukkit.getWorld(worldId) ?: throw IllegalStateException("World with id $worldId is not loaded")
}

object NoopGameWorld : GameWorld(UUID(0L, 0L))