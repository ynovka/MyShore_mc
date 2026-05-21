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
import org.bukkit.World
import ru.ynovka.myShore.game.pillars.states.PillarsInProgress


object PillarsEvents : Listener {

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
    }

    @EventHandler
    fun onPlayerFall(e: PlayerMoveEvent) {
        println(e.player.world.name)
        if (!e.player.world.isPillarsWorld()) return
        println("onPlayerFall 2")
        if (e.player.gameMode != GameMode.SURVIVAL) return
        println("onPlayerFall 3")
        if (e.to.y > 0.0) return
        println("onPlayerFall 4")

        val game = e.player.uniqueId.currentPillarsGame() ?: return
        println("onPlayerFall 5")
        if (game.fsm.current !is PillarsInProgress) return
        println("onPlayerFall 6")
        game.movePlayerToSpectator(e.player, SpectatorReason.ELIMINATED)

        // todo сообщение о вылете
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        if (!e.player.world.isPillarsWorld()) return

        val game = e.player.uniqueId.currentPillarsGame() ?: return
        game.movePlayerToSpectator(e.player, SpectatorReason.ELIMINATED)

        // todo сообщение о вылете
    }

    private fun World.isPillarsWorld() = name.startsWith("myshore_pillars_")
}