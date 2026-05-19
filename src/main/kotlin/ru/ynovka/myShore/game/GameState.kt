package ru.ynovka.myShore.game

abstract class GameState<P : GamePlayer, W : GameWorld, G : Game<P, W>>(protected val game: G) {
    open fun onEnterState() {}
    open fun onExitState() {}

    open fun onPlayerJoin(gamePlayer: P) {}
    open fun canPlayerJoin(gamePlayer: P): Boolean = true

    open fun onPlayerReconnect(gamePlayer: P) = onPlayerJoin(gamePlayer)

    open fun onPlayerBecomeSpectator(gamePlayer: P, reason: SpectatorReason) {}
    open fun canPlayerBecomeSpectator(gamePlayer: P, reason: SpectatorReason): Boolean = true

    open fun onPlayerLeave(gamePlayer: P) {}
    open fun onSpectatorJoin(gameSpectator: P) {}
}