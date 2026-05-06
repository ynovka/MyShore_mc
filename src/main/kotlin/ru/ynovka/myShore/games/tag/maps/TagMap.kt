package ru.ynovka.myShore.games.tag.maps

import ru.ynovka.myShore.games.tag.maps.impl.TagMountainTrackMap
import ru.ynovka.myShore.games.tag.maps.impl.TagJungleMap
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.concurrent.ThreadLocalRandom
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import ru.ynovka.myShore.MyShore.Companion.inst


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

fun TagMap.teleportPlayers(game: TagGame): CompletableFuture<Void> {
    val teleports = game.gamePlayers.map { tagPlayer ->
        teleport(tagPlayer.player, game)
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

/**
 * Асинхронный телепорт игрока на позицию, соответствующую его роли на этой карте.
 * Перед телепортом игрок принудительно возвращается в visibility-группу игры,
 * чтобы прямые телепорты на карту не обходили логику видимости.
 * [onComplete] вызывается в main thread после успешного телепорта.
 */
fun TagMap.teleport(player: Player, game: TagGame, onComplete: () -> Unit = {}): CompletableFuture<Boolean> {
    game.gameVisibilityGroup.addViewer(player.uniqueId)

    val role = game.findPlayer(player)?.role
        ?: if (game.fsm.current is ru.ynovka.myShore.games.tag.states.TagInProgressState ||
            game.fsm.current is ru.ynovka.myShore.games.tag.states.TagPreparing
        ) {
            TagPlayerRoles.SPECTATOR
        } else {
            TagPlayerRoles.UNDEFINED
        }

    val destination = when (role) {
        TagPlayerRoles.UNDEFINED,
        TagPlayerRoles.VICTIM -> victimSpawns
            .shuffled()
            .firstOrNull { it.toLocation().getNearbyPlayers(1.0).isEmpty() }
            ?.toLocation()
            ?: victimSpawns.random().toLocation()

        TagPlayerRoles.HUNTER -> hunterSpawn.toLocation()

        TagPlayerRoles.SPECTATOR,
        TagPlayerRoles.SPECTATOR_VICTIM -> {
            val hunter = game.gamePlayers
                .firstOrNull { it.role == TagPlayerRoles.HUNTER }
                ?.player
            hunter?.location ?: victimSpawns.random().toLocation()
        }
    }

    return player.teleportAsync(destination).thenApply { success ->
        if (success) {
            Bukkit.getScheduler().runTask(inst, Runnable { onComplete() })
        }
        success
    }
}
