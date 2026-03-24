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

    fun playerJoin(player: P) {
        game.players.add(player)
        current.onPlayerJoin(game, player)
    }

    fun playerReconnect(player: P) {
        // todo game.players.add(player)
        current.onPlayerReconnect(game, player)
    }

    fun playerLeave(player: P) {
        game.players.remove(player)
        current.onPlayerLeave(game, player)
    }
}