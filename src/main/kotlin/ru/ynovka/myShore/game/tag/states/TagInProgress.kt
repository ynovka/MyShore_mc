package ru.ynovka.myShore.game.tag.states

import ru.ynovka.myShore.game.tag.utils.ActionbarHunterDistance
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.gameUtils.BossbarTimer
import ru.ynovka.myShore.game.tag.TagPlayer
import ru.ynovka.myShore.game.tag.TagGame
import ru.ynovka.myShore.game.GameWorld
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.utils.canMove


// 40-100 сек (сам геймплей салочек)
class TagInProgressState(game: TagGame) : GameState<TagPlayer, GameWorld, TagGame>(game) {
    lateinit var timer: BossbarTimer.BossbarTimerHandle

    override fun onEnterState() {
        game.gamePlayers.asPlayers().forEach { player ->
            scheduler.schedule {
                player.canMove(true)
            }.entity(player).once()
        }
        ActionbarHunterDistance.startRendering(game, this)
        timer = BossbarTimer.startCountdownTimer(
            time = 40,
            game = game,
            state = this,
            onCompletion = { game, _ ->
                game.fsm.transitionTo(TagFinishing(game))
            }
        )
    }

    override fun canPlayerJoin(gamePlayer: TagPlayer): Boolean = false
}