package ru.ynovka.myShore.antiCheat

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.MyShore.Companion.scheduler
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import java.util.UUID


object AntiCheat {
    private val playersPackets = ConcurrentHashMap<UUID, Int>(128)

    fun register() {
        scheduler.schedule {
            playersPackets.entries.removeIf { (uuid, tick) ->
                val player = Bukkit.getPlayer(uuid) ?: return@removeIf true

                if (Bukkit.getCurrentTick() - tick > 60) {
                    if (player.location.block.getRelative(BlockFace.DOWN).type == Material.AIR) {
                        player.kick(Component.text("Packet timeout"))
                        true
                    }
                    false
                } else false
            }
        }
            .after(60L, Clock.TICKS)
            .repeatEvery(60L, Clock.TICKS)
    }

    fun handlePacket(player: Player) {
        playersPackets[player.uniqueId] = Bukkit.getCurrentTick()
    }

    fun remove(player: Player) {
        playersPackets.remove(player.uniqueId)
    }
}