package ru.ynovka.myShore.visibilityGroup

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class VisibilityGroup {

    private val members: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    companion object {
        // Глобальный реестр: UUID -> группа, в которой состоит игрок
        private val playerGroupIndex: ConcurrentHashMap<UUID, VisibilityGroup> =
            ConcurrentHashMap()

        fun onPlayerQuit(uuid: UUID) {
            playerGroupIndex[uuid]?.removeMember(uuid)
        }

        fun UUID.getVisiblePlayers(include: Boolean = false): MutableSet<UUID> {
            return playerGroupIndex[this]
                ?.members
                ?.filter { include || it != this }
                ?.toMutableSet()
                ?: mutableSetOf()
        }

        fun Player.getVisiblePlayers(include: Boolean = false) = this.uniqueId.getVisiblePlayers(include)
    }

    // --- Public API ---

    fun addViewer(uuid: UUID) {
        // Выходим из старой группы если есть
        playerGroupIndex[uuid]?.removeMember(uuid)

        members.add(uuid)
        playerGroupIndex[uuid] = this

        applyOnMain {
            val newPlayer = Bukkit.getPlayer(uuid) ?: return@applyOnMain
            for (memberId in members) {
                if (memberId == uuid) continue
                val member = Bukkit.getPlayer(memberId) ?: continue
                // Новый видит всех в группе
                newPlayer.showPlayer(MyShore.Companion.inst, member)
                // Все в группе видят нового
                member.showPlayer(MyShore.Companion.inst, newPlayer)
            }
        }
    }

    fun removeViewer(uuid: UUID) {
        if (!members.contains(uuid)) return
        removeMember(uuid)
    }

    fun hasViewer(uuid: UUID): Boolean = uuid in members

    fun getViewers(): Set<UUID> = members.toHashSet()

    // --- Internal ---

    private fun removeMember(uuid: UUID) {
        members.remove(uuid)
        playerGroupIndex.remove(uuid)

        applyOnMain {
            val removedPlayer = Bukkit.getPlayer(uuid)
            for (memberId in members) {
                val member = Bukkit.getPlayer(memberId) ?: continue
                // Ушедший больше не видит участников
                removedPlayer?.hidePlayer(MyShore.Companion.inst, member)
                // Участники больше не видят ушедшего
                member.hidePlayer(MyShore.Companion.inst, removedPlayer ?: continue)
            }
        }
    }

    private fun applyOnMain(block: () -> Unit) {
        if (Bukkit.isPrimaryThread()) {
            block()
        } else {
            Bukkit.getScheduler().runTask(MyShore.Companion.inst, Runnable { block() })
        }
    }
}