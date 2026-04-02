package ru.ynovka.myShore.games.worldDomination.entity

import org.bukkit.Location
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer

enum class CountrySkin(
    val loc: Location
) {
    DEFAULT(Location(WDGame.world, 1000.0, 100.0, 0.0)),
    MODERN(Location(WDGame.world, 2000.0, 100.0, 0.0));

    companion object {
        fun get(player: WDPlayer): CountrySkin {
            // todo получаем из данных игрока выбранный им скин
            return DEFAULT
        }
    }
}
