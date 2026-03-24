package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.text.clearActionBar
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagPlayer
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.utils.canMove
import net.kyori.adventure.text.Component
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import kotlin.math.roundToInt
import org.bukkit.Bukkit
import ru.ynovka.myShore.text.ComponentDecorator


// 40-100 сек (сам геймплей салочек)
object TagInProgressState : GameState<TagPlayer> {

    private var bossBar: BossBar? = null

    override fun onEnter(game: Game<TagPlayer>) {
        val tagGame = game as TagGame
        tagGame.players.forEach { it.player.canMove(true) }
        startHunterDistanceRenderer(tagGame)
        startCountdown(tagGame)
    }

    override fun onPlayerJoin(game: Game<TagPlayer>, player: TagPlayer) {
        val tagGame = game as TagGame
        player.player.setupAsSpectator(tagGame)
        bossBar?.addPlayer(player.player)
    }

    // ---------- отображение расстояния до охотника ----------

    private fun startHunterDistanceRenderer(game: TagGame) {
        game.players.forEach { it.player.clearActionBar() }

        val hunter = game.players.firstOrNull { it.role == TagPlayerRoles.HUNTER }?.player ?: return

        fun tick() {
            if (game.fsm.current != TagInProgressState) return

            game.players
                .filter { it.role == TagPlayerRoles.VICTIM }
                .forEach { tagPlayer ->
                    val distance = ((tagPlayer.player.location.distance(hunter.location) * 10).roundToInt() / 10.0)
                    tagPlayer.player.sendActionBar(
                        ComponentDecorator.addBackground(
                            Component.translatable(
                                "bar.myshore.tag.distance_to_hunter",
                                Component.text(distance)
                            ),
                            tagPlayer.player
                        )
                    )
                }

            game.scheduler.runTaskLater(inst, Runnable { tick() }, 2L)
        }

        tick()
    }

    // ---------- таймер игры ----------

    private fun startCountdown(game: TagGame) {
        game.totalTime = 40

        val bar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID).also { bar ->
            bar.progress = 1.0
            bar.isVisible = true
            game.players.forEach(bar::addPlayer)
        }
        bossBar = bar

        fun tick() {
            if (game.fsm.current != TagInProgressState) {
                bar.removeAll()
                bossBar = null
                return
            }

            if (game.remainingTime > 0) {
                // todo перевод
                bar.setTitle("Осталось ${game.remainingTime} секунд")
                bar.progress = game.remainingTime.toDouble() / game.totalTime.toDouble()
                game.remainingTime--
                game.scheduler.runTaskLater(inst, Runnable { tick() }, 20L)
            } else {
                bar.removeAll()
                bossBar = null
                game.fsm.transitionTo(TagFinishingState)
            }
        }

        tick()
    }
}

// Расширение, чтобы addPlayer принимал TagPlayer напрямую
private fun BossBar.addPlayer(tagPlayer: TagPlayer) = addPlayer(tagPlayer.player)