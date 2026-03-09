package ru.ynovka.myShore.games.tag.maps

import net.kyori.adventure.text.TranslatableComponent
import ru.ynovka.myShore.games.tag.maps.impl.JungleMap
import ru.ynovka.myShore.games.tag.maps.impl.MountainTrackMap
import java.util.concurrent.ThreadLocalRandom
import ru.ynovka.myShore.utils.MapSpawn


interface TagGameMap {

    val mapId: String
    val mapName: TranslatableComponent
    val authors: List<String>

    val hunterSpawn: MapSpawn
    val victimSpawns: List<MapSpawn>

    companion object Registry {

        private val maps = listOf(
            JungleMap,
            MountainTrackMap,
        )

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
