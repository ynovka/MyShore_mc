package ru.ynovka.myShore.game.worldDomination

import ru.ynovka.myShore.game.GameWorldOld
import org.bukkit.Location
import org.bukkit.Bukkit
import org.bukkit.World


object WDWorldOld : GameWorldOld {
    const val WORLD_NAME = "world_domination"

    override val world: World
        get() = Bukkit.getWorld(WORLD_NAME)
            ?: error("World '$WORLD_NAME' is not loaded")

    val hubLoc: Location
        get() = Location(world, 0.0, 100.0, 0.0)

    val unLoc: Location
        get() = Location(world, 1000.0, 100.0, 0.0)
}
