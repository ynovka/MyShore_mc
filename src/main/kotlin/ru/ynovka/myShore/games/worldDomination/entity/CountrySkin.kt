package ru.ynovka.myShore.games.worldDomination.entity

import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDWorld
import org.bukkit.Location


enum class CountrySkin(
    val loc: Location
) {
    DEFAULT(Location(WDWorld.world, 2000.0, 100.0, 0.0)),
    MODERN(Location(WDWorld.world, 3000.0, 100.0, 0.0));

    companion object {
        fun get(player: WDPlayer): CountrySkin {
            // todo получаем из данных игрока выбранный им скин
            return DEFAULT
        }
    }
}
