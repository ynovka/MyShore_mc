package ru.ynovka.myShore.games

class GameFSM<P : GamePlayer>(initial: GameState<P>) {

    var current: GameState<P> = initial
        private set

    private lateinit var game: Game<P>

    fun start(game: Game<P>) {
        this.game = game
        current.onEnter(game)
    }

    fun transitionTo(next: GameState<P>) {
        current.onExit(game)
        current = next
        current.onEnter(game)
    }

    fun playerJoin(p: P) {
        current.onPlayerJoin(game, p)
    }

    fun playerReconnect(p: P) {
        current.onPlayerReconnect(game, p)
    }

    fun playerLeave(p: P) {
        current.onPlayerLeave(game, p)
    }

    fun canPlayerJoin(p: P): Boolean {
        if (game.isFull()) return false
        return current.canPlayerJoin(game, p)
    }

    fun spectatorJoin(s: P) {
        current.onSpectatorJoin(game, s)
    }
}