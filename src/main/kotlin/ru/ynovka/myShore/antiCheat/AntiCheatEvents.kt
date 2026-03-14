package ru.ynovka.myShore.antiCheat

import com.github.darksoulq.abyssallib.server.event.custom.server.PacketReceiveEvent
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket
import com.github.darksoulq.abyssallib.server.event.SubscribeEvent
import org.bukkit.event.player.PlayerQuitEvent

object AntiCheatEvents {
    @SubscribeEvent
    fun onPacket(e: PacketReceiveEvent) {
        if (e.packet !is ServerboundClientTickEndPacket) return
        AntiCheat.handlePacket(e.player)
    }

    @SubscribeEvent
    fun onPlayerQuit(e: PlayerQuitEvent) {
        AntiCheat.remove(e.player)
    }
}