package ru.ynovka.myShore.games.tag.maps.impl

import ru.ynovka.myShore.games.tag.maps.TagMap
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.games.tag.states.TagFinishing
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.hasVictims
import ru.ynovka.myShore.games.tag.teleport
import ru.ynovka.myShore.games.tag.currentTagGame
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.block.Block
import org.bukkit.Material
import org.bukkit.GameMode
import org.bukkit.Bukkit
import ru.ynovka.myShore.games.tag.findPlayer
import kotlin.math.sqrt


object TagMountainTrackMap : TagMap {

    override val mapId = "tag_mountain_track"
    override val mapName = Component.translatable("name.myshore.tag.map.mountain_track")

    override val authors = listOf(
        "Ynovka",
        "Vo1tron196"
    )

    override val hunterSpawn = MapSpawn("tag_mountain_track", -49.5, 96.0, 66.5, 175f, 0f)

    override val victimSpawns = listOf(
        MapSpawn("tag_mountain_track", -50.5, 96.0, 12.5, 15f, 0f),
        MapSpawn("tag_mountain_track", -48.5, 96.0, 12.5, 15f, 0f),
        MapSpawn("tag_mountain_track", -46.5, 96.0, 13.5, 30f, 0f),
        MapSpawn("tag_mountain_track", -45.5, 96.0, 15.5, 35f, 0f)
    )

    override fun registerEvents() = Events.register()

    object Events {
        private data class BlockPos(val x: Int, val y: Int, val z: Int)
        private val lightNearbyPositions = HashSet<BlockPos>()

        fun register() {
            val world = Bukkit.getServer().getWorld(mapId)!!
            precomputeLightPositions(world)

            inst.server.scheduler.runTaskTimer(inst, Runnable {
                world.players.forEach { player ->
                    if (player.gameMode == GameMode.CREATIVE) return@forEach

                    if (player.y <= 65) {
                        val game = player.currentTagGame() ?: return@forEach
                        val tagPlayer = game.findPlayer(player) ?: return@forEach

                        when (tagPlayer.role) {
                            TagPlayerRoles.HUNTER -> {
                                player.gameMode = GameMode.SPECTATOR
                                player.clearActivePotionEffects()
                                tagPlayer.role = TagPlayerRoles.SPECTATOR
                                game.fsm.transitionTo(TagFinishing(game))
                            }
                            TagPlayerRoles.VICTIM -> {
                                player.gameMode = GameMode.SPECTATOR
                                player.clearActivePotionEffects()
                                tagPlayer.role = TagPlayerRoles.SPECTATOR_VICTIM

                                val msg = Component.translatable(
                                    "msg.myshore.tag.player.fall_death",
                                    Component.text(player.name)
                                )
                                game.gamePlayers.forEach { it.player.sendMessage(msg) }

                                if (!game.hasVictims()) {
                                    game.fsm.transitionTo(TagFinishing(game))
                                } else {
                                    game.totalTime += 20
                                }
                            }
                            TagPlayerRoles.UNDEFINED -> {
                                game.map.teleport(player, game) {
                                    player.gameMode = GameMode.ADVENTURE
                                }
                            }
                            else -> Unit
                        }
                    }

                    val result = BlockPos(
                        player.location.block.x,
                        player.location.block.y,
                        player.location.block.z
                    ) in lightNearbyPositions
                    if (result) {
                        if (player.isSneaking) {
                            player.teleport(player.location.add(0.0, 0.0025, 0.0))
                        }
                        val dx = -90.0 - player.x
                        val dz = 40.0 - player.z
                        val len = sqrt(dx * dx + 1.0 + dz * dz)
                        player.velocity = player.velocity.add(
                            org.bukkit.util.Vector(dx/len * 1.25, 1.0/len * 1.25, dz/len * 1.25)
                        )
                    }
                }
            }, 0L, 5L)
        }

        private fun precomputeLightPositions(world: org.bukkit.World) {
            val xRange = -79..-26
            val yRange = 55..118
            val zRange = -4..89

            val chunkXRange = (xRange.first shr 4)..(xRange.last shr 4)
            val chunkZRange = (zRange.first shr 4)..(zRange.last shr 4)

            for (cx in chunkXRange) for (cz in chunkZRange) {
                world.getChunkAt(cx, cz).also { it.load(true) }
            }

            for (x in xRange) for (z in zRange) for (y in yRange) {
                val block = world.getBlockAt(x, y, z)
                if (!isLightActive(block)) continue

                lightNearbyPositions.add(BlockPos(x, y, z))
                ADJACENT_FACES.forEach { face ->
                    block.getRelative(face).also { rel ->
                        lightNearbyPositions.add(BlockPos(rel.x, rel.y, rel.z))
                    }
                }
            }
        }

        private fun isLightActive(block: Block): Boolean {
            if (block.type != Material.LIGHT) return false
            return (block.blockData as? org.bukkit.block.data.Levelled)?.level == 0
        }

        private val ADJACENT_FACES = arrayOf(
            BlockFace.WEST, BlockFace.EAST,
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
        )
    }
}