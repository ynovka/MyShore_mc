package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.games.tag.TagGameStates
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.utils.Utils.asPlayer
import ru.ynovka.myShore.utils.clearActionBar
import ru.ynovka.myShore.games.tag.TagGame
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.utils.canMove
import org.bukkit.entity.Player
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import kotlin.math.roundToInt
import org.bukkit.Bukkit


// 40-100 сек (сам геймплей салочек)
object InProgressState : GameState {

    // BossBar хранится здесь, чтобы его можно было корректно убрать при смене состояния.
    // Если в будущем будет несколько одновременных игр — вынести в TagMiniGame.
    private var bossBar: BossBar? = null

    override fun onStateStart(game: TagGame) {
        game.lobby.members.asPlayers().forEach { it.canMove(true) }
        startHunterDistanceRenderer(game)
        startCountdown(game)
    }

    override fun onPlayerJoin(game: TagGame, player: Player) {
        // Новые игроки во время IN_PROGRESS — только зрители
        player.setupAsSpectator(game)
        // Добавляем в BossBar, если он уже создан
        bossBar?.addPlayer(player)
    }

    // ---------- отображение расстояния до охотника ----------

    private fun startHunterDistanceRenderer(game: TagGame) {
        game.lobby.members.asPlayers().forEach { it.clearActionBar() }

        val hunterUuid = game.players
            .filterValues { it == TagPlayerRoles.HUNTER }
            .keys.firstOrNull() ?: return
        val hunter = hunterUuid.asPlayer() ?: return

        fun tick() {
            if (game.state != TagGameStates.IN_PROGRESS) return

            game.players
                .filterValues { it == TagPlayerRoles.VICTIM }
                .keys.asPlayers()
                .forEach { victim ->
                    val distance = ((victim.location.distance(hunter.location) * 10).roundToInt() / 10.0)
                    victim.sendActionBar(
                        Component.translatable(
                            "bar.myshore.tag.distance_to_hunter",
                            Component.text(distance)
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
            game.lobby.members.asPlayers().forEach(bar::addPlayer)
        }
        bossBar = bar

        fun tick() {
            if (game.state != TagGameStates.IN_PROGRESS) {
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
                game.transitionTo(TagGameStates.FINISHING)
            }
        }

        tick()
    }
}