package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.PillarsGame.Companion.currentPillarsGame
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import ru.ynovka.myShore.game.SpectatorReason
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.GameMode


object PillarsEvents : Listener {

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler
    fun onPlayerFall(e: PlayerMoveEvent) {
        if (!e.player.isInPillarsWorld()) return
        if (e.player.gameMode != GameMode.SURVIVAL) return
        if (e.to.y > 0.0) return

        val game = e.player.uniqueId.currentPillarsGame() ?: return
        game.movePlayerToSpectator(e.player, SpectatorReason.ELIMINATED)
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        if (!e.player.isInPillarsWorld()) return

        val game = e.player.uniqueId.currentPillarsGame() ?: return
        game.movePlayerToSpectator(e.player, SpectatorReason.ELIMINATED)
    }

    private fun Player.isInPillarsWorld() = world.name.startsWith("pillars_")
}