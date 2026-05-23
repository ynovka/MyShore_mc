package ru.ynovka.myShore.game.tag.maps.impl

import ru.ynovka.myShore.game.tag.TagGame.Companion.currentTagGame
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import io.papermc.paper.datacomponent.item.MapId.mapId
import ru.ynovka.myShore.game.tag.states.TagFinishing
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.tag.TagPlayerRoles
import ru.ynovka.myShore.game.tag.maps.MapSpawn
import ru.ynovka.myShore.game.tag.maps.TagMap
import ru.ynovka.myShore.game.tag.findPlayer
import ru.ynovka.myShore.game.tag.hasVictims
import ru.ynovka.myShore.game.tag.teleport
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.block.Block
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Bukkit
import ru.ynovka.myShore.game.tag.states.TagInProgressState
import kotlin.concurrent.timer
import kotlin.math.sqrt


object TagMountainTrackMap : TagMap() {

    override val name = "tag_mountain_track"
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
            scheduler.schedule {
                Bukkit.getServer().getWorld(name)?.let { world ->
                    precomputeLightPositions(world)
                }
            }
                .after(20L, Clock.TICKS)
                .once()

            scheduler.schedule {
                Bukkit.getServer().getWorld(name)?.players?.forEach { player ->
                    if (player.gameMode == GameMode.CREATIVE) return@forEach

                    if (player.y <= 65) {
                        val game = player.uniqueId.currentTagGame() ?: return@forEach
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
                                    val state = game.fsm.current
                                    if (state is TagInProgressState) {
                                        state.timer.addTime(20)
                                    }
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
            }
                .after(20L, Clock.TICKS)
                .repeatEvery(5L, Clock.TICKS)
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
