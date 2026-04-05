package ru.ynovka.myShore.games

abstract class GameState<P : GamePlayer>(protected val game: Game<P>) {
    open fun onEnter() {}
    open fun onExit() {}
    open fun onPlayerJoin(player: P) {}
    open fun onPlayerReconnect(player: P) = onPlayerJoin(player)
    open fun onPlayerLeave(player: P) {}
    open fun canPlayerJoin(player: P): Boolean = true
    open fun onSpectatorJoin(spectator: P) {}
}