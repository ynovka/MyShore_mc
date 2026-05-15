package ru.ynovka.myShore.games

class GameFSM<P : GamePlayer, W : GameWorld>(initial: GameState<P, W, *>) {
    var current: GameState<P, W, *> = initial
        private set

    fun start() = current.onEnterState()

    fun transitionTo(next: GameState<P, W, *>) {
        current.onExitState()
        current = next
        current.onEnterState()
    }

    fun playerJoin(p: P) = current.onPlayerJoin(p)
    fun playerReconnect(p: P) = current.onPlayerReconnect(p)
    fun playerLeave(p: P) = current.onPlayerLeave(p)
    fun canPlayerJoin(p: P) = current.canPlayerJoin(p)
    fun spectatorJoin(s: P) = current.onSpectatorJoin(s)
    fun playerBecomeSpectator(p: P, reason: SpectatorReason) = current.onPlayerBecomeSpectator(p, reason)
    fun canPlayerBecomeSpectator(p: P, reason: SpectatorReason) = current.canPlayerBecomeSpectator(p, reason)
}