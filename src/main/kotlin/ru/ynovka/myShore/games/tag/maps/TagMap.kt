package ru.ynovka.myShore.games.tag.maps

import ru.ynovka.myShore.games.tag.maps.impl.TagMountainTrackMap
import ru.ynovka.myShore.games.tag.maps.impl.TagJungleMap
import net.kyori.adventure.text.TranslatableComponent
import java.util.concurrent.ThreadLocalRandom
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.utils.MapSpawn
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture


enum class TagMaps(
    val mapProvider: () -> TagMap
) {
    RANDOM({ TagMap.Registry.random() }),
    JUNGLE({ TagMap.Registry.byId("tag_jungle") }),
    MOUNTAIN_TRACK({ TagMap.Registry.byId("tag_mountain_track") });
}

interface TagMap {

    val mapId: String
    val mapName: TranslatableComponent
    val authors: List<String>

    val hunterSpawn: MapSpawn
    val victimSpawns: List<MapSpawn>

    /** Вызывается после телепорта игроков в начале игры */
    fun onGameStart(game: TagGame) {}

    /** Вызывается при завершении игры */
    fun onGameEnd(game: TagGame) {}

    /** Вызывается когда игрок присоединяется к игре */
    fun onPlayerJoin(game: TagGame, player: Player) {}

    /** Вызывается когда игрок покидает игру */
    fun onPlayerLeave(game: TagGame, player: Player) {}

    /** Вызывается вместе с TagEvents.register() */
    fun registerEvents() {}

    /** Вызывается вместе с TagItems.register() */
    fun registerItems() {}

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
            return maps.firstOrNull { it.mapId == id }
                ?: throw IllegalArgumentException("No TagGame map with ID $id")
        }
    }
}

fun TagMap.teleportPlayers(game: TagGame) {
    val victims    = game.gamePlayers.filter { it.role == TagPlayerRoles.VICTIM || it.role == TagPlayerRoles.UNDEFINED }
    val hunters    = game.gamePlayers.filter { it.role == TagPlayerRoles.HUNTER }
    val spectators = game.gamePlayers.filter { it.role == TagPlayerRoles.SPECTATOR || it.role == TagPlayerRoles.SPECTATOR_VICTIM }

    val shuffledVictimSpawns = victimSpawns.shuffled().toMutableList()
    val teleports = mutableListOf<CompletableFuture<Boolean>>()

    // Victims
    victims.forEachIndexed { index, tagPlayer ->
        val spawn = shuffledVictimSpawns.getOrNull(index)?.toLocation()
            ?: victimSpawns.random().toLocation()
        teleports += tagPlayer.player.teleportAsync(spawn)
    }

    // Hunters
    hunters.forEach { tagPlayer ->
        teleports += tagPlayer.player.teleportAsync(hunterSpawn.toLocation())
    }

    // Spectators follow the hunter
    val hunterLocation = hunters.firstOrNull()?.player?.location
    spectators.forEach { tagPlayer ->
        val loc = hunterLocation ?: victimSpawns.random().toLocation()
        teleports += tagPlayer.player.teleportAsync(loc)
    }

    CompletableFuture.allOf(*teleports.toTypedArray())
}