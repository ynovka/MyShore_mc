package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupForWaiting
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.tag.*
import org.bukkit.entity.Player
import org.bukkit.Sound


// Ожидание игроков (нужно хотя бы 2)
object WaitingForPlayersState : GameState {
    override fun onStateStart(game: TagGame) {
        game.lobby.members.asPlayers().forEach { player ->
            player.setupForWaiting(game)
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f)
        }
    }

    override fun onPlayerJoin(game: TagGame, player: Player) {
        player.setupForWaiting(game)

        if (game.lobby.members.size >= 2) {
            game.transitionTo(TagGameStates.VOTING)
        }
        // Если игрок один — actionbar уже установлен в setupForWaiting
    }
}
