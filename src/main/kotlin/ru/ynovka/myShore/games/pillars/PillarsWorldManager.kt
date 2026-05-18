package ru.ynovka.myShore.games.pillars

import ru.ynovka.myShore.games.pillars.Pillar.Companion.TELEPORT_Y
import ru.ynovka.myShore.utils.VoidWorldGenerator
import org.bukkit.WorldCreator
import org.bukkit.Location
import java.util.UUID


object PillarsWorldManager {

    fun createWorld(): PillarsWorld {
        val world = PillarsWorld(
            WorldCreator.name("pillars_${UUID.randomUUID()}")
                .generateStructures(false)
                .generator(VoidWorldGenerator())
                .createWorld()?.uid
                ?: error("Failed to create pillars world")
        )

        return world
    }

    fun spawnPlayer(pGame: PillarsGame, pPlayer: PillarsPlayer) {
        if (pPlayer.playerId in pGame.gameWorld.pillars.map { it.owner }) {
            println("zzzzzzzzzzzzzzzzzzzzz")
            return
        }
        
        val world = pGame.gameWorld.world

        // Ищём свободное место для спавна столба
        val loc = pGame.allocator.gen.generate(pGame, pPlayer.playerId)
        // Создаём столб на нужных координтах
        pGame.pillar.gen.generate(world, loc)
        // Создаём коробку игрока
        PillarsPlayerBox.create(world, pPlayer, loc)
        // телепортируем игрока в коробку
        pPlayer.player.teleportAsync(
            Location(
                world,
                loc.x + 0.5, TELEPORT_Y, loc.z + 0.5
            )
        )
    }

    fun spawnPlayers(pGame: PillarsGame) = pGame.gamePlayers.forEach { spawnPlayer(pGame, it) }

}