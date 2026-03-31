package ru.ynovka.myShore.games.tag.maps.impl

import ru.ynovka.myShore.games.tag.maps.TagMap
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.games.tag.states.TagFinishing
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.hasVictims
import ru.ynovka.myShore.games.tag.teleport
import ru.ynovka.myShore.games.tag.currentTagGame
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.MapSpawn
import org.bukkit.block.BlockFace
import org.bukkit.block.Block
import org.bukkit.Material
import org.bukkit.Location
import org.bukkit.GameMode
import org.bukkit.Bukkit
import ru.ynovka.myShore.games.tag.findPlayer


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
        fun register() {
            val world = Bukkit.getServer().getWorld(mapId)!!
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
                                game.fsm.transitionTo(TagFinishing)
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
                                    game.fsm.transitionTo(TagFinishing)
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

                    val block = player.location.block
                    val b = hasLightBlock(listOf(
                        block,
                        block.getRelative(BlockFace.WEST),
                        block.getRelative(BlockFace.EAST),
                        block.getRelative(BlockFace.NORTH),
                        block.getRelative(BlockFace.SOUTH),
                        block.getRelative(BlockFace.NORTH_EAST),
                        block.getRelative(BlockFace.NORTH_WEST),
                        block.getRelative(BlockFace.SOUTH_EAST),
                        block.getRelative(BlockFace.SOUTH_WEST)
                    ))
                    if (b) {
                        if (player.isSneaking) {
                            player.teleport(player.location.add(0.0, 0.0025, 0.0))
                        }
                        val vec = Location(world, -90.0, player.y.plus(1), 40.0).clone().toVector()
                            .subtract(player.location.toVector()).normalize().multiply(1.25)
                        player.velocity = player.velocity.add(vec)
                    }
                }
            }, 0L, 5L)
        }

        private fun hasLightBlock(blocks: List<Block>): Boolean {
            blocks.forEach { block ->
                if (block.type != Material.LIGHT) return@forEach
                val data = block.blockData
                if (data.lightEmission == 0) return true
            }
            return false
        }
    }
}