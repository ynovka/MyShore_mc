package ru.ynovka.myShore.games.tag.maps.impl

import ru.ynovka.myShore.games.tag.maps.TagGameMap
import ru.ynovka.myShore.utils.MapSpawn


object MountainTrackMap : TagGameMap {

    override val mapId = "tag_mountain_track"
    override val mapName = "Горная трасса"

    override val authors = listOf(
        "Ynovka",
        "Vo1tron196"
    )

    override val hunterSpawn = MapSpawn("tag_mountain_track", -55.5, 96.0, 0.0, 0f, 0f)

    override val victimSpawns = listOf(
        MapSpawn("tag_mountain_track", -55.5, 96.0, 0.0, 0f, 0f),
    )
}
