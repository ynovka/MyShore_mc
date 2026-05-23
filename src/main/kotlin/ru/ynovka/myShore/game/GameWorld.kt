package ru.ynovka.myShore.game

import ru.ynovka.myShore.MyShore.Companion.PLUGIN_ID
import net.thenextlvl.worlds.generator.GeneratorType
import java.util.concurrent.CompletableFuture
import net.thenextlvl.worlds.preset.Preset
import net.thenextlvl.worlds.WorldsAccess
import net.thenextlvl.worlds.Dimension
import net.kyori.adventure.key.Key
import net.thenextlvl.worlds.Level
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.GameRules
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.World


abstract class GameWorld {
    abstract val name: String

    val key by lazy { Key.key(PLUGIN_ID, name.lowercase()) }

    fun get() = WorldsAccess.access().server.getWorld(key)

    fun getOrCreate(): CompletableFuture<World> {
        val access = WorldsAccess.access()

        get()?.let {
            return CompletableFuture.completedFuture(it)
        }

        if (access.worldRegistry.isRegistered(key)) {
            return access.load(key)
        }

        val preset = Preset.THE_VOID.toBuilder()
            .features(false)
            .build()

        val level = Level.builder(key)
            .dimension(Dimension.OVERWORLD)
            .generatorType(GeneratorType.FLAT.with(preset))
            .structures(false)
            .build()

        return access.create(level).thenApply { world ->
            access.worldRegistry.register(level, true)
            configureWorld(world)
        }
    }

    fun delete(): CompletableFuture<Boolean> {
        val access = WorldsAccess.access()
        val world = get() ?: return CompletableFuture.completedFuture(false)

        return access.delete(world).exceptionally { throwable ->
            throwable.printStackTrace()
            false
        }
    }

    fun teleport(player: Player, location: Location): CompletableFuture<Boolean> {
        return getOrCreate().thenCompose { world ->
            val target = location.clone().apply { this.world = world }
            player.teleportAsync(target)
        }.whenComplete { _, throwable ->
            if (throwable != null) throwable.printStackTrace()
        }
    }

    private fun configureWorld(world: World): World {
        world.time = 6000L
        world.setGameRule(GameRules.ADVANCE_TIME, false)

        world.setStorm(false)
        world.isThundering = false
        world.setGameRule(GameRules.ADVANCE_WEATHER, false)

        world.setGameRule(GameRules.SPECTATORS_GENERATE_CHUNKS, false)

        world.difficulty = Difficulty.EASY

        return world
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