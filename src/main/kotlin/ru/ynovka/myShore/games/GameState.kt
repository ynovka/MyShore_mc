package ru.ynovka.myShore.games

interface GameState<P : GamePlayer> {
    fun onEnter(game: Game<P>) {}
    fun onExit(game: Game<P>) {}
    fun onPlayerJoin(game: Game<P>,  player: P) {}
    fun onPlayerReconnect(game: Game<P>,  player: P) = onPlayerJoin(game, player)
    fun onPlayerLeave(game: Game<P>, player: P) {}
    fun canPlayerJoin(game: Game<P>, player: P): Boolean = true
    fun onSpectatorJoin(game: Game<P>,  spectator: P) {}
}