package ru.ynovka.myShore.games.tag.maps

import ru.ynovka.myShore.games.tag.maps.impl.TagMountainTrackMap
import ru.ynovka.myShore.games.tag.maps.impl.TagJungleMap
import net.kyori.adventure.text.TranslatableComponent
import java.util.concurrent.ThreadLocalRandom
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.utils.MapSpawn
import org.bukkit.entity.Player
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.utils.Utils.asPlayers
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

    /** Вызывается когда игрок покидает игру */
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
    val players = game.players.keys.asPlayers()

    val victimPlayers = players.filter {
        val role = game.players[it.uniqueId]
        role == TagPlayerRoles.VICTIM || role == TagPlayerRoles.UNDEFINED
    }

    val hunterPlayers = players.filter {
        game.players[it.uniqueId] == TagPlayerRoles.HUNTER
    }

    val spectatorPlayers = players.filter {
        val role = game.players[it.uniqueId]
        role == TagPlayerRoles.SPECTATOR || role == TagPlayerRoles.SPECTATOR_VICTIM
    }

    val shuffledVictimSpawns = victimSpawns.shuffled().toMutableList()

    val teleports = mutableListOf<CompletableFuture<Boolean>>()

    // Victims
    victimPlayers.forEachIndexed { index, player ->
        val spawn = shuffledVictimSpawns.getOrNull(index) ?.toLocation() ?: victimSpawns.random().toLocation()

        teleports += player.teleportAsync(spawn)
    }

    // Hunters
    hunterPlayers.forEach { player ->
        teleports += player.teleportAsync(hunterSpawn.toLocation())
    }

    // Spectators
    val hunter = hunterPlayers.firstOrNull()

    spectatorPlayers.forEach { player ->
        val loc = hunter?.location ?: victimSpawns.random().toLocation()
        teleports += player.teleportAsync(loc)
    }

    CompletableFuture.allOf(*teleports.toTypedArray())
}