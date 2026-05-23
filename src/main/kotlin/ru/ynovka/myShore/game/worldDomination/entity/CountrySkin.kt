package ru.ynovka.myShore.game.worldDomination.entity

import ru.ynovka.myShore.game.worldDomination.WDPlayer
import ru.ynovka.myShore.game.worldDomination.WDWorld
import org.bukkit.Location


enum class CountrySkin(
    val loc: Location
) {
    DEFAULT(Location(WDWorld.get(), 2000.0, 100.0, 0.0)),
    MODERN(Location(WDWorld.get(), 3000.0, 100.0, 0.0));

    companion object {
        fun get(player: WDPlayer): CountrySkin {
            // todo получаем из данных игрока выбранный им скин
            return DEFAULT
        }
    }
}
