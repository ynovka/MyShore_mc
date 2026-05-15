package ru.ynovka.myShore.games.tag

import ru.ynovka.myShore.games.tag.TagGame.Companion.currentTagGame
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import ru.ynovka.myShore.Database.tagCaughtsRepository
import dev.jorel.commandapi.kotlindsl.playerExecutor
import ru.ynovka.myShore.games.tag.maps.TagMaps
import ru.ynovka.myShore.MyShore.Companion.inst
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

                val task = inst.server.scheduler.runTaskTimer(inst, Runnable {
                    points.forEach { pt ->
                        val color = Color.fromRGB(pt.r, pt.g, pt.b)
                        player.spawnParticle(
                            Particle.DUST,
                            pt.position.x, pt.position.y, pt.position.z,
                            1, 0.0, 0.0, 0.0, 0.0,
                            Particle.DustOptions(color, 1.0f),
                        )
                    }
                }, 0L, 4L)

                inst.server.scheduler.runTaskLater(inst, Runnable {
                    task.cancel()
                }, 20L * 10)

                player.sendMessage("Отображено ${points.size} точек тепловой карты.")
            }
        }
    }
}