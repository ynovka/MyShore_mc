package ru.ynovka.myShore.game

import ru.ynovka.myShore.MyShore.Companion.PLUGIN_ID
import net.thenextlvl.worlds.generator.GeneratorType
import java.util.concurrent.CompletableFuture
import net.thenextlvl.worlds.preset.Preset
import net.thenextlvl.worlds.WorldsAccess
import net.thenextlvl.worlds.Dimension
import net.kyori.adventure.key.Key
import net.thenextlvl.worlds.Level
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.World


interface GameWorldOld {
    val world: World
}

abstract class GameWorld {
    abstract val name: String

    val key by lazy { Key.key(PLUGIN_ID, name.lowercase()) }

    fun getOrCreate(): CompletableFuture<World> {
        val access = WorldsAccess.access()

        access.server.getWorld(key)?.let {
            return CompletableFuture.completedFuture(it)
        }

        if (access.worldRegistry.isRegistered(key)) {
            return access.load(key)
        }

        val level = Level.builder(key)
            .dimension(Dimension.OVERWORLD)
            .generatorType(GeneratorType.FLAT.with(Preset.THE_VOID))
            .structures(false)
            .build()

        return access.create(level).thenApply { world ->
            access.worldRegistry.register(level, true)
            world
        }
    }

    fun teleport(player: Player, location: Location): CompletableFuture<Boolean> {
        return getOrCreate().thenCompose { world ->
            val target = location.clone().apply { this.world = world }
            player.teleportAsync(target)
        }
    }
}

class HubGameWorld(
    gameName: String
) : GameWorld() {
    override val name = "hub_$gameName"

    fun teleportToSpawn(player: Player): CompletableFuture<Boolean> {
        return teleport(
            player,
            Location(null, 0.5, 100.0, 0.5)
        )
    }
}