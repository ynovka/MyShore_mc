package ru.ynovka.myShore.games.tag.maps


enum class TagGameMaps(
    val mapProvider: () -> TagGameMap
) {
    RANDOM({ TagGameMap.Registry.random() }),
    JUNGLE({ TagGameMap.Registry.byId("tag_jungle") }),
    MOUNTAIN_TRACK({ TagGameMap.Registry.byId("tag_mountain_track") });
}
