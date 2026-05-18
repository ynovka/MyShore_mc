package ru.ynovka.myShore.games.tag.states

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.text.actionBar.clearActionBar
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.text.ComponentDecorator
import ru.ynovka.myShore.games.tag.TagPlayer
import ru.ynovka.myShore.games.tag.TagGame
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.games.GameWorld
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.utils.canMove
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import kotlin.math.roundToInt
import org.bukkit.Bukkit


// 40-100 сек (сам геймплей салочек)
class TagInProgressState(game: TagGame) : GameState<TagPlayer, GameWorld, TagGame>(game) {

    private var bossBar: BossBar? = null

    override fun onEnterState() {
        game.gamePlayers.forEach { it.player.canMove(true) }
        startHunterDistanceRenderer(game)
        startCountdown(game)
    }

    override fun onPlayerJoin(gamePlayer: TagPlayer) {
        gamePlayer.player.setupAsSpectator(game)
        bossBar?.addPlayer(gamePlayer.player)
    }

    // ---------- отображение расстояния до охотника ----------

    private fun startHunterDistanceRenderer(game: TagGame) {
        game.gamePlayers.forEach { it.player.clearActionBar() }

        val hunter = game.gamePlayers.firstOrNull { it.role == TagPlayerRoles.HUNTER }?.player ?: return

        fun tick() {
            if (game.fsm.current !is TagInProgressState) return

            game.gamePlayers
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

            game.scheduler.schedule { tick() }
                .sync()
                .after(2L, Clock.TICKS)
                .once()
        }

        tick()
    }

    // ---------- таймер игры ----------

    private fun startCountdown(game: TagGame) {
        game.totalTime = 40

        val bar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID).also { bar ->
            bar.progress = 1.0
            bar.isVisible = true
            game.gamePlayers.forEach(bar::addPlayer)
        }
        bossBar = bar

        fun tick() {
            if (game.fsm.current !is TagInProgressState) {
                bar.removeAll()
                bossBar = null
                return
            }

            if (game.remainingTime > 0) {
                // todo перевод
                bar.setTitle("Осталось ${game.remainingTime} секунд")
                bar.progress = game.remainingTime.toDouble() / game.totalTime.toDouble()
                game.remainingTime--
                game.scheduler.schedule { tick() }
                    .sync()
                    .after(20L, Clock.TICKS)
                    .once()
            } else {
                bar.removeAll()
                bossBar = null
                game.fsm.transitionTo(TagFinishing(game))
            }
        }

        tick()
    }
}

// Расширение, чтобы addPlayer принимал TagPlayer напрямую
private fun BossBar.addPlayer(tagPlayer: TagPlayer) = addPlayer(tagPlayer.player)