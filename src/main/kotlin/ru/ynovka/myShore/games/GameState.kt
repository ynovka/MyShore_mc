package ru.ynovka.myShore.games

abstract class GameState<P : GamePlayer, G : Game<P>>(protected val game: G) {
    open fun onEnterState() {}
    open fun onExitState() {}
    open fun onPlayerJoin(gamePlayer: P) {}
    open fun onPlayerReconnect(gamePlayer: P) = onPlayerJoin(gamePlayer)
    open fun onPlayerLeave(gamePlayer: P) {}
    open fun canPlayerJoin(gamePlayer: P): Boolean = true
    open fun onSpectatorJoin(gameSpectator: P) {}
}