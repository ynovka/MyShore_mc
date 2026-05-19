package ru.ynovka.myShore.game.tag

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.tag.TagGame.Companion.currentTagGame
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import ru.ynovka.myShore.Database.tagCaughtsRepository
import dev.jorel.commandapi.kotlindsl.playerExecutor
import ru.ynovka.myShore.game.tag.maps.TagMaps
import ru.ynovka.myShore.MyShore.Companion.scheduler
import org.bukkit.Particle
import org.bukkit.Color


object TagCommands {
    fun register() {
        commandAPICommand("tag_caughts") {
            playerExecutor { player, _ ->
                val world = player.world

                if (!world.name.startsWith("tag_")) {
                    player.sendMessage("Эта команда доступна только на картах Салочек.")
                    return@playerExecutor
                }

                val rawName = world.name
                    .substringAfter("tag_")
                    .uppercase()

                val tagMap = runCatching { TagMaps.valueOf(rawName) }.getOrElse {
                    player.sendMessage("Неизвестная карта: $rawName")
                    return@playerExecutor
                }

                val points = tagCaughtsRepository.getVictimHeatmap(
                    playerName = player.name,
                    map        = tagMap,
                    limit      = 1000,
                )

                if (points.isEmpty()) {
                    player.sendMessage("Нет данных для отображения.")
                    return@playerExecutor
                }

                // Достаточно найти TagGame через GameManager — lobby больше не нужен
                player.currentTagGame() ?: return@playerExecutor

                val task = scheduler.schedule {
                    points.forEach { pt ->
                        val color = Color.fromRGB(pt.r, pt.g, pt.b)
                        player.spawnParticle(
                            Particle.DUST,
                            pt.position.x, pt.position.y, pt.position.z,
                            1, 0.0, 0.0, 0.0, 0.0,
                            Particle.DustOptions(color, 1.0f),
                        )
                    }
                }
                    .sync()
                    .repeatEvery(4L, Clock.TICKS)

                scheduler.schedule {
                    task.cancel()
                }
                    .sync()
                    .after(20L * 10, Clock.TICKS)
                    .once()

                player.sendMessage("Отображено ${points.size} точек тепловой карты.")
            }
        }
    }
}