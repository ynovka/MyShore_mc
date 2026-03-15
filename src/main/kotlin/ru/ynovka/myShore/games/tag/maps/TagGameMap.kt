package ru.ynovka.myShore.games.tag.maps

import ru.ynovka.myShore.games.tag.maps.impl.MountainTrackMap
import ru.ynovka.myShore.games.tag.maps.impl.JungleMap
import net.kyori.adventure.text.TranslatableComponent
import java.util.concurrent.ThreadLocalRandom
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.utils.MapSpawn
import org.bukkit.entity.Player


interface TagGameMap {

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
            JungleMap,
            MountainTrackMap)
            private set

        fun random(): TagGameMap {
            require(maps.isNotEmpty()) { "No TagGame maps registered" }
            return maps[ThreadLocalRandom.current().nextInt(maps.size)]
        }

        fun byId(id: String): TagGameMap {
            return maps.firstOrNull { it.mapId == id }
                ?: throw IllegalArgumentException("No TagGame map with ID $id")
        }
    }
}
