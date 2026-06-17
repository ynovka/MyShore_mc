package ru.ynovka.myShore.game

import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.scheduler


@PublishedApi
internal object GamePlayerPreparation {
    @PublishedApi
    internal fun reset(player: Player) {
        scheduler.schedule {
            player.inventory.clear()
            player.clearActivePotionEffects()
            player.allowFlight = false
            player.isFlying = false
            player.isInvulnerable = false
            player.isCollidable = true
            player.canPickupItems = true
        }.entity(player).once()
    }

    @PublishedApi
    internal fun resetAll(players: Iterable<Player>) {
        players.forEach(::reset)
    }
}
