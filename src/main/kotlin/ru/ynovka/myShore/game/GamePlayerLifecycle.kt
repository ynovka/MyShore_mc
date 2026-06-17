package ru.ynovka.myShore.game

import org.bukkit.GameMode
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.gameUtils.SpectatorVisibility
import ru.ynovka.myShore.game.gameUtils.VisibilityGroup
import ru.ynovka.myShore.hub.HubItems
import java.util.UUID


internal class GamePlayerLifecycle<P : GamePlayer, W : GameWorld>(
    private val roster: GameRoster<P>,
    private val visibilityGroup: VisibilityGroup,
    private val fsm: () -> GameFSM<P, W>,
    private val isDestroyed: () -> Boolean,
    private val maxPlayers: () -> Int,
    private val onPlayerJoined: (P) -> Unit,
    private val onPlayerReconnected: (P) -> Unit,
    private val onPlayerLeft: (P) -> Unit,
    private val onPlayerBecameSpectator: (P, SpectatorReason) -> Unit
) {
    private val spectatorVisibility = SpectatorVisibility(visibilityGroup)

    fun canAcceptNewPlayer(playerId: UUID): Boolean {
        if (roster.isFull(maxPlayers())) return false

        val gamePlayer = roster.createDetachedPlayer(playerId)
        return fsm().canPlayerJoin(gamePlayer)
    }

    fun onPlayerJoin(playerId: UUID) {
        if (isDestroyed()) return

        val gamePlayer = roster.getOrCreatePlayer(playerId)

        if (gamePlayer in roster.activePlayers || gamePlayer in roster.spectatorPlayers) return

        visibilityGroup.addViewer(playerId)

        if (roster.exitedPlayers.remove(gamePlayer)) {
            if (!fsm().canPlayerReconnect(gamePlayer)) {
                movePlayerToSpectator(gamePlayer)
                fsm().spectatorJoin(gamePlayer)
                return
            }

            roster.activePlayers.add(gamePlayer)
            refreshSpectatorVisibility()

            fsm().playerReconnect(gamePlayer)
            onPlayerReconnected(gamePlayer)
            return
        }

        if (roster.exitedSpectatorPlayers.remove(gamePlayer)) {
            if (movePlayerToSpectator(gamePlayer)) {
                fsm().spectatorJoin(gamePlayer)
            }
            return
        }

        val canJoin = !roster.isFull(maxPlayers()) && fsm().canPlayerJoin(gamePlayer)

        if (!canJoin) {
            movePlayerToSpectator(gamePlayer, SpectatorReason.GAME_FULL)
            fsm().spectatorJoin(gamePlayer)
            return
        }

        roster.activePlayers.add(gamePlayer)
        refreshSpectatorVisibility()

        fsm().playerJoin(gamePlayer)
        onPlayerJoined(gamePlayer)
    }

    fun onPlayerLeave(playerId: UUID) {
        if (isDestroyed()) return

        visibilityGroup.removeViewer(playerId)

        val player = roster.getPlayer(playerId) ?: return

        val wasActive = roster.activePlayers.remove(player)
        val wasSpectator = roster.spectatorPlayers.remove(player)

        if (!wasActive && !wasSpectator) return

        refreshSpectatorVisibility()

        if (wasActive) {
            roster.exitedPlayers.add(player)

            fsm().playerLeave(player)
            onPlayerLeft(player)
        }

        if (wasSpectator) {
            roster.exitedSpectatorPlayers.add(player)
        }

        roster.forgetOnlinePlayer(playerId)
    }

    fun movePlayerToSpectator(
        gamePlayer: P,
        reason: SpectatorReason = SpectatorReason.UNKNOWN
    ): Boolean {
        if (isDestroyed()) return false

        val player = roster.rememberPlayer(gamePlayer)

        if (player in roster.spectatorPlayers) return false
        if (!fsm().canPlayerBecomeSpectator(player, reason)) return false

        roster.activePlayers.remove(player)
        roster.exitedPlayers.remove(player)
        roster.spectatorPlayers.add(player)
        refreshSpectatorVisibility()

        applySpectatorMode(player)

        fsm().playerBecomeSpectator(player, reason)
        onPlayerBecameSpectator(player, reason)

        return true
    }

    fun moveSpectatorsToActive() {
        if (roster.spectatorPlayers.isEmpty()) return

        val movedPlayers = roster.spectatorPlayers.toList()

        roster.activePlayers += movedPlayers
        roster.spectatorPlayers.clear()

        movedPlayers.forEach(::applyActiveMode)
        refreshSpectatorVisibility()
    }

    fun refreshSpectatorVisibility() {
        spectatorVisibility.refresh(
            spectatorPlayers = roster.spectatorPlayers
        )
    }

    private fun applySpectatorMode(player: P) {
        player.withOnlinePlayer { bukkitPlayer ->
            scheduler.schedule {
                bukkitPlayer.gameMode = GameMode.ADVENTURE
                bukkitPlayer.allowFlight = true
                bukkitPlayer.isFlying = true
                bukkitPlayer.isInvulnerable = true
                bukkitPlayer.isCollidable = false
                bukkitPlayer.canPickupItems = false
                bukkitPlayer.clearActivePotionEffects()
                bukkitPlayer.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        -1,
                        0,
                        false,
                        false,
                        false
                    )
                )
                bukkitPlayer.inventory.clear()
                bukkitPlayer.inventory.setItem(0, SpectatorItems.teleportCompass.getStack(bukkitPlayer))
                bukkitPlayer.inventory.setItem(8, HubItems.hubTeleport.getStack(bukkitPlayer))
            }.entity(bukkitPlayer).once()
        }
    }

    private fun applyActiveMode(player: P) {
        player.withOnlinePlayer { bukkitPlayer ->
            scheduler.schedule {
                bukkitPlayer.gameMode = GameMode.ADVENTURE
                bukkitPlayer.allowFlight = false
                bukkitPlayer.isFlying = false
                bukkitPlayer.isInvulnerable = false
                bukkitPlayer.isCollidable = true
                bukkitPlayer.canPickupItems = true
                bukkitPlayer.clearActivePotionEffects()
                bukkitPlayer.inventory.clear()
            }.entity(bukkitPlayer).once()
        }
    }
}
