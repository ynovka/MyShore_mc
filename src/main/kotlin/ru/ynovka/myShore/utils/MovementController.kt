package ru.ynovka.myShore.utils

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.Bukkit
import java.util.UUID


object MovementController : Listener {

    private const val AREA_HALF_SIZE = 0.2
    private const val SPRING = 0.45
    private const val DAMPING = 0.05
    private const val UPDATE_INTERVAL_TICKS = 4L

    private val blockMovement: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val areaMovement: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    private val blockAnchors: MutableMap<UUID, Anchor> = ConcurrentHashMap()
    private val areaAnchors: MutableMap<UUID, Anchor> = ConcurrentHashMap()

    private var tickerStarted = false

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
        startTicker()
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        releaseBlockAnchor(e.player.uniqueId)
        releaseAreaRestriction(e.player.uniqueId)
    }

    private fun startTicker() {
        if (tickerStarted) return
        tickerStarted = true

        scheduler.schedule {
            tickMovement()
        }.global().repeatEvery(UPDATE_INTERVAL_TICKS, Clock.TICKS)
    }

    private fun tickMovement() {
        val playerIds = HashSet<UUID>(blockMovement.size + areaMovement.size)
        playerIds.addAll(blockMovement)
        playerIds.addAll(areaMovement)

        for (uuid in playerIds) {
            val player = Bukkit.getPlayer(uuid)

            if (player == null || !player.isOnline || !player.isValid) {
                releaseBlockAnchor(uuid)
                releaseAreaRestriction(uuid)
                continue
            }

            scheduler.schedule {
                if (!player.isOnline || !player.isValid) {
                    releaseBlockAnchor(uuid)
                    releaseAreaRestriction(uuid)
                    return@schedule
                }

                applyBlockAnchorForce(player)
                applyAreaBoundaryForce(player)
            }.entity(player).once()
        }
    }

    private fun applyBlockAnchorForce(player: Player) {
        val uuid = player.uniqueId

        if (uuid !in blockMovement) return

        val anchor = blockAnchors[uuid] ?: run {
            releaseBlockAnchor(uuid)
            return
        }

        val centerX = anchor.blockX + 0.5
        val centerZ = anchor.blockZ + 0.5

        val loc = player.location
        val velocity = player.velocity

        velocity.x = velocity.x * DAMPING + (centerX - loc.x) * SPRING
        velocity.z = velocity.z * DAMPING + (centerZ - loc.z) * SPRING

        player.velocity = velocity
    }

    private fun applyAreaBoundaryForce(player: Player) {
        val uuid = player.uniqueId

        if (uuid !in areaMovement) return

        val anchor = areaAnchors[uuid] ?: run {
            releaseAreaRestriction(uuid)
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
    }

    fun anchorPlayerToCurrentBlock(player: Player) {
        val uuid = player.uniqueId

        blockAnchors[uuid] = Anchor(
            player.location.blockX,
            player.location.blockZ
        )

        blockMovement.add(uuid)
    }

    fun releaseBlockAnchor(player: Player) {
        releaseBlockAnchor(player.uniqueId)
    }

    private fun releaseBlockAnchor(uuid: UUID) {
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

        areaMovement.add(uuid)
    }

    fun releaseAreaRestriction(player: Player) {
        releaseAreaRestriction(player.uniqueId)
    }

    private fun releaseAreaRestriction(uuid: UUID) {
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
