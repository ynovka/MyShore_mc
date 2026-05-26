package ru.ynovka.myShore.utils

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.Location
import java.util.UUID


object MovementController : Listener {

    private const val AREA_HALF_SIZE = 0.2
    private const val SPRING = 0.45
    private const val DAMPING = 0.05

    val blockMovement: MutableMap<UUID, Boolean> = ConcurrentHashMap()

    private val blockAnchors: MutableMap<UUID, Anchor> = ConcurrentHashMap()
    private val areaAnchors: MutableMap<UUID, Anchor> = ConcurrentHashMap()
    private val areaMovement: MutableMap<UUID, Boolean> = ConcurrentHashMap()

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        releaseBlockAnchor(e.player)
        releaseAreaRestriction(e.player)
    }

    private fun applyBlockAnchorForce(player: Player) {
        val uuid = player.uniqueId

        if (!player.isOnline || !player.isValid) {
            releaseBlockAnchor(player)
            return
        }

        if (blockMovement[uuid] != true) return

        val anchor = blockAnchors[uuid] ?: run {
            releaseBlockAnchor(player)
            return
        }

        val centerX = anchor.blockX + 0.5
        val centerZ = anchor.blockZ + 0.5

        val loc = player.location
        val velocity = player.velocity

        velocity.x = velocity.x * DAMPING + (centerX - loc.x) * SPRING
        velocity.z = velocity.z * DAMPING + (centerZ - loc.z) * SPRING

        player.velocity = velocity

        scheduler.schedule {
            applyBlockAnchorForce(player)
        }.entity(player).once()
    }

    private fun applyAreaBoundaryForce(player: Player) {
        val uuid = player.uniqueId

        if (!player.isOnline || !player.isValid) {
            releaseAreaRestriction(player)
            return
        }

        if (areaMovement[uuid] != true) return

        val anchor = areaAnchors[uuid] ?: run {
            releaseAreaRestriction(player)
            return
        }

        val centerX = anchor.blockX + 0.5
        val centerZ = anchor.blockZ + 0.5

        val loc = player.location
        val velocity = player.velocity

        val dx = loc.x - centerX
        val dz = loc.z - centerZ

        var modified = false

        if (dx > AREA_HALF_SIZE) {
            velocity.x = velocity.x * DAMPING +
                    (centerX + AREA_HALF_SIZE - loc.x) * SPRING
            modified = true
        } else if (dx < -AREA_HALF_SIZE) {
            velocity.x = velocity.x * DAMPING +
                    (centerX - AREA_HALF_SIZE - loc.x) * SPRING
            modified = true
        }

        if (dz > AREA_HALF_SIZE) {
            velocity.z = velocity.z * DAMPING +
                    (centerZ + AREA_HALF_SIZE - loc.z) * SPRING
            modified = true
        } else if (dz < -AREA_HALF_SIZE) {
            velocity.z = velocity.z * DAMPING +
                    (centerZ - AREA_HALF_SIZE - loc.z) * SPRING
            modified = true
        }

        if (modified) player.velocity = velocity

        scheduler.schedule {
            applyAreaBoundaryForce(player)
        }.entity(player).once()
    }

    fun anchorPlayerToCurrentBlock(player: Player) {
        val uuid = player.uniqueId

        blockAnchors[uuid] = Anchor(
            player.location.blockX,
            player.location.blockZ
        )

        val alreadyActive = blockMovement.put(uuid, true) == true
        if (alreadyActive) return

        scheduler.schedule {
            applyBlockAnchorForce(player)
        }.entity(player).once()
    }

    fun releaseBlockAnchor(player: Player) {
        val uuid = player.uniqueId
        blockMovement.remove(uuid)
        blockAnchors.remove(uuid)
    }

    fun restrictPlayerToArea(player: Player, origin: Location? = null) {
        val uuid = player.uniqueId

        val source = origin ?: player.location
        areaAnchors[uuid] = Anchor(
            source.blockX,
            source.blockZ
        )

        val alreadyActive = areaMovement.put(uuid, true) == true
        if (alreadyActive) return

        scheduler.schedule {
            applyAreaBoundaryForce(player)
        }.entity(player).once()
    }

    fun releaseAreaRestriction(player: Player) {
        val uuid = player.uniqueId
        areaMovement.remove(uuid)
        areaAnchors.remove(uuid)
    }

    private data class Anchor(
        val blockX: Int,
        val blockZ: Int
    )
}

fun Player.restrictToBlockCenter(value: Boolean) {
    if (value) {
        MovementController.anchorPlayerToCurrentBlock(this)
    } else {
        MovementController.releaseBlockAnchor(this)
    }
}

fun Player.restrictToBlock(value: Boolean, origin: Location? = null) {
    if (value) {
        MovementController.restrictPlayerToArea(this, origin)
    } else {
        MovementController.releaseAreaRestriction(this)
    }
}