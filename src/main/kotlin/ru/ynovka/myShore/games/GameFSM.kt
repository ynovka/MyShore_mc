package ru.ynovka.myShore.games

class GameFSM<P : GamePlayer>(initial: GameState<P, *>) {
    var current: GameState<P, *> = initial
        private set

    fun start() = current.onEnterState()

    fun transitionTo(next: GameState<P, *>) {
        current.onExitState()
        current = next
        current.onEnterState()
    }

    fun playerJoin(p: P) = current.onPlayerJoin(p)
    fun playerReconnect(p: P) = current.onPlayerReconnect(p)
    fun playerLeave(p: P) = current.onPlayerLeave(p)
    fun canPlayerJoin(p: P) = current.canPlayerJoin(p)
    fun spectatorJoin(s: P) = current.onSpectatorJoin(s)
}