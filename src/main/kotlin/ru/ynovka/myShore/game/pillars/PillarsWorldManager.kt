package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.Pillar.Companion.TELEPORT_Y
import ru.ynovka.myShore.utils.VoidWorldGenerator
import org.bukkit.WorldCreator
import org.bukkit.Location
import ru.ynovka.myShore.MyShore.Companion.scheduler
import java.util.UUID
import java.util.concurrent.CompletableFuture


object PillarsWorldManager {

    fun createWorld(): PillarsWorldOld {
        val future = CompletableFuture<PillarsWorldOld>()
        scheduler.schedule {
            val world = PillarsWorldOld(
                WorldCreator.name("pillars_${UUID.randomUUID()}")
                    .generateStructures(false)
                    .generator(VoidWorldGenerator())
                    .createWorld()?.uid
                    ?: error("Failed to create pillars world")
            )
            future.complete(world)
        }.global().once()
        return future.get()
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
        PillarsPlayerBox.create(world, loc)
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