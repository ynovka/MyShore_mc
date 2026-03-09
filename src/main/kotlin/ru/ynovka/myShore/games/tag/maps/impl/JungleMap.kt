package ru.ynovka.myShore.games.tag.maps.impl

import net.kyori.adventure.text.Component
import ru.ynovka.myShore.games.tag.maps.TagGameMap
import ru.ynovka.myShore.utils.MapSpawn


object JungleMap : TagGameMap {

    override val mapId = "tag_jungle"
    override val mapName = Component.translatable("name.myshore.tag.map.jungle")

    override val authors = listOf(
        "Ynovka",
        "_JuliA_"
    )

    override val hunterSpawn = MapSpawn(
        "tag_jungle",
        -2.5, 106.0, -42.5,
        0f, 0f
    )

    override val victimSpawns = listOf(
        MapSpawn("tag_jungle", 3.5, 102.0, 0.5, 180f, 0f),
        MapSpawn("tag_jungle", 0.5, 101.0, 0.5, 180f, 0f),
        MapSpawn("tag_jungle", -2.5, 101.0, 0.5, 180f, 0f),
        MapSpawn("tag_jungle", -6.5, 101.0, -2.5, 180f, 0f),
        MapSpawn("tag_jungle", -4.5, 101.0, -1.5, 180f, 0f)
    )
}
