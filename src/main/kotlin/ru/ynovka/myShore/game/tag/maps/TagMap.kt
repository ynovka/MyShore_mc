package ru.ynovka.myShore.game.tag.maps

import ru.ynovka.myShore.game.tag.maps.impl.TagMountainTrackMap
import ru.ynovka.myShore.game.tag.maps.impl.TagJungleMap
import net.kyori.adventure.text.TranslatableComponent
import ru.ynovka.myShore.game.tag.TagPlayerRoles
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.CompletableFuture
import ru.ynovka.myShore.game.tag.teleport
import ru.ynovka.myShore.game.tag.TagGame
import ru.ynovka.myShore.game.GameWorld
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.Bukkit


enum class TagMaps(
    val mapProvider: () -> TagMap
) {
    RANDOM({ TagMap.random() }),
    JUNGLE({ TagMap.byId("tag_jungle") }),
    MOUNTAIN_TRACK({ TagMap.byId("tag_mountain_track") });
}

abstract class TagMap : GameWorld() {

    abstract val mapName: TranslatableComponent
    abstract val authors: List<String>

    abstract val hunterSpawn: MapSpawn
    abstract val victimSpawns: List<MapSpawn>

    /**
     * Вызывается после телепорта игроков в начале игры
     */
    open fun onGameStart(game: TagGame) {}

    /**
     * Вызывается при завершении игры
     */
    open fun onGameEnd(game: TagGame) {}

    /**
     * Вызывается когда игрок присоединяется к игре
     */
    open fun onPlayerJoin(game: TagGame, player: Player) {}

    /**
     * Вызывается когда игрок покидает игру
     */
    open fun onPlayerLeave(game: TagGame, player: Player) {}

    /**
     * Вызывается вместе с TagEvents.register()
     */
    open fun registerEvents() {}

    /**
     * Вызывается вместе с TagItems.register()
     */
    open fun registerItems() {}

    companion object Registry {

        val maps = listOf(
            TagJungleMap,
            TagMountainTrackMap
        )

        fun random(): TagMap {
            require(maps.isNotEmpty()) { "No TagGame maps registered" }
            return maps[ThreadLocalRandom.current().nextInt(maps.size)]
        }

        fun byId(id: String): TagMap {
            return maps.firstOrNull { it.name == id }
                ?: throw IllegalArgumentException("No TagGame map with ID $id")
        }
    }
}

fun TagMap.teleportPlayers(game: TagGame): CompletableFuture<Void> {
    val victims = game.gamePlayers.filter {
        it.role == TagPlayerRoles.VICTIM || it.role == TagPlayerRoles.UNDEFINED
    }
    val hunters = game.gamePlayers.filter { it.role == TagPlayerRoles.HUNTER }
    val spectators = game.gamePlayers.filter {
        it.role == TagPlayerRoles.SPECTATOR || it.role == TagPlayerRoles.SPECTATOR_VICTIM
    }

    val shuffledVictimSpawns = victimSpawns.shuffled().toMutableList()
    val teleports = mutableListOf<CompletableFuture<Boolean>>()

    victims.forEachIndexed { index, tagPlayer ->
        val spawn = shuffledVictimSpawns.getOrNull(index)?.toLocation()
            ?: victimSpawns.random().toLocation()
        teleports += teleport(tagPlayer.player, game, spawn)
    }

    hunters.forEach { tagPlayer ->
        teleports += teleport(tagPlayer.player, game, hunterSpawn.toLocation())
    }

    val hunterLocation = hunters.firstOrNull()?.player?.location
    spectators.forEach { tagPlayer ->
        val spawn = hunterLocation ?: victimSpawns.random().toLocation()
        teleports += teleport(tagPlayer.player, game, spawn)
    }

    return CompletableFuture.allOf(*teleports.toTypedArray())
}

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
