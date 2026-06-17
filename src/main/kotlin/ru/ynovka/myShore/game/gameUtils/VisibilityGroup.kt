package ru.ynovka.myShore.game.gameUtils

import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.event.EventPriority
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import ru.ynovka.myShore.MyShore
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import java.util.UUID


class VisibilityGroup {

    private val members: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val hiddenTargetsByViewer: MutableMap<UUID, MutableSet<UUID>> = ConcurrentHashMap()

    companion object {
        private val playerGroupIndex = ConcurrentHashMap<UUID, VisibilityGroup>()
        private val membershipLock = Any()

        fun onPlayerQuit(uuid: UUID) {
            val group: VisibilityGroup
            val oldMembers: Set<UUID>

            synchronized(membershipLock) {
                group = playerGroupIndex[uuid] ?: return
                oldMembers = group.members.toSet() - uuid
                group.removeMemberLocked(uuid)
            }

            oldMembers.forEach { memberId ->
                group.hideBoth(uuid, memberId)
            }
        }

        fun UUID.getVisiblePlayers(include: Boolean = false): Set<UUID> {
            val group: VisibilityGroup

            synchronized(membershipLock) {
                group = playerGroupIndex[this] ?: return emptySet()
                val hiddenTargets = group.hiddenTargetsByViewer[this].orEmpty()

                return group.members
                    .filterTo(mutableSetOf()) { (include || it != this) && it !in hiddenTargets }
            }
        }

        fun Player.getVisiblePlayers(include: Boolean = false): Set<UUID> =
            uniqueId.getVisiblePlayers(include)
    }

    fun addViewer(uuid: UUID) {
        val oldMembers: Set<UUID>
        val newMembers: Set<UUID>

        synchronized(membershipLock) {
            val oldGroup = playerGroupIndex[uuid]

            oldMembers = oldGroup
                ?.members
                ?.toSet()
                ?.minus(uuid)
                .orEmpty()

            oldGroup?.removeMemberLocked(uuid)

            members.add(uuid)
            playerGroupIndex[uuid] = this

            newMembers = members.toSet() - uuid
        }

        oldMembers.forEach { memberId ->
            hideBoth(uuid, memberId)
        }

        newMembers.forEach { memberId ->
            showBoth(uuid, memberId)
        }
    }

    fun removeViewer(uuid: UUID) {
        val oldMembers: Set<UUID>

        synchronized(membershipLock) {
            if (uuid !in members) return

            oldMembers = members.toSet() - uuid
            removeMemberLocked(uuid)
        }

        oldMembers.forEach { memberId ->
            hideBoth(uuid, memberId)
        }
    }

    fun showFor(viewerId: UUID, targetId: UUID) {
        if (viewerId == targetId) return

        var shouldShow = false

        synchronized(membershipLock) {
            if (viewerId !in members || targetId !in members) return

            hiddenTargetsByViewer[viewerId]?.let { hiddenTargets ->
                shouldShow = hiddenTargets.remove(targetId)

                if (hiddenTargets.isEmpty()) {
                    hiddenTargetsByViewer.remove(viewerId)
                }
            }
        }

        if (!shouldShow) return

        showOneWay(viewerId, targetId)
    }

    fun hideFor(viewerId: UUID, targetId: UUID) {
        if (viewerId == targetId) return

        val shouldHide: Boolean

        synchronized(membershipLock) {
            if (viewerId !in members || targetId !in members) return

            shouldHide = hiddenTargetsByViewer
                .getOrPut(viewerId) { ConcurrentHashMap.newKeySet() }
                .add(targetId)
        }

        if (!shouldHide) return

        hideOneWay(viewerId, targetId)
    }

    fun clear() {
        val oldMembers: Set<UUID>

        synchronized(membershipLock) {
            if (members.isEmpty()) return

            oldMembers = members.toSet()

            oldMembers.forEach { uuid ->
                playerGroupIndex.remove(uuid, this)
            }

            members.clear()
            hiddenTargetsByViewer.clear()
        }

        oldMembers.forEach { a ->
            oldMembers.forEach { b ->
                if (a != b) hideOneWay(a, b)
            }
        }
    }

    fun hasViewer(uuid: UUID): Boolean = uuid in members

    fun getViewers(): Set<UUID> = members.toSet()

    private fun removeMemberLocked(uuid: UUID) {
        members.remove(uuid)
        hiddenTargetsByViewer.remove(uuid)
        hiddenTargetsByViewer.values.forEach { it.remove(uuid) }

        playerGroupIndex.remove(uuid, this)
    }

    private fun showBoth(a: UUID, b: UUID) {
        showOneWay(a, b)
        showOneWay(b, a)
    }

    private fun hideBoth(a: UUID, b: UUID) {
        hideOneWay(a, b)
        hideOneWay(b, a)
    }

    private fun showOneWay(viewerId: UUID, targetId: UUID) {
        val viewer = Bukkit.getPlayer(viewerId) ?: return

        MyShore.scheduler.schedule {
            val currentViewer = Bukkit.getPlayer(viewerId) ?: return@schedule
            val target = Bukkit.getPlayer(targetId) ?: return@schedule

            currentViewer.showPlayer(inst, target)
        }.entity(viewer).once()
    }

    private fun hideOneWay(viewerId: UUID, targetId: UUID) {
        val viewer = Bukkit.getPlayer(viewerId) ?: return

        MyShore.scheduler.schedule {
            val currentViewer = Bukkit.getPlayer(viewerId) ?: return@schedule
            val target = Bukkit.getPlayer(targetId) ?: return@schedule

            currentViewer.hidePlayer(inst, target)
        }.entity(viewer).once()
    }
}

object VisibilityGroupEvents : Listener {

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        VisibilityGroup.onPlayerQuit(e.player.uniqueId)
    }

}
