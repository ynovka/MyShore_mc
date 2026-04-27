package ru.ynovka.myShore.games.worldDomination.states

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.GameState
import org.bukkit.Location
import ru.ynovka.myShore.games.worldDomination.entity.Country.Companion.getFormattedName
import ru.ynovka.myShore.text.actionBar.ActionBar
import ru.ynovka.myShore.utils.BossBarTimer
import java.time.Duration
import java.util.UUID
import kotlin.math.atan2


/**
 * Этап совещания ООН
 * Длится от 9 до 15 минут (кол-во стран * 90 секунд)
 * (возможно нужно дать 10 секунд на подтверждения выступления, если не подтвердить выступление -
 * страну переместит в конец выступления, работает 1 раз за совещание, иначе речь пропускается)
 * В этот период каждой стране даётся 1 минута на любую речь
 * Порядок стран для выступления:
 * - прилетело больше бомб
 * - уровень развития
 * - название по алфавиту
 */
class WDUNMeeting(game: WDGame) : GameState<WDPlayer, WDGame>(game) {
    var speakingCountry: Int? = null
    var nowSpeaking: MutableSet<UUID> = mutableSetOf()

    override fun onEnterState() {
        game.gamePlayers.forEach { wdPlayer ->
            game.gameVisibilityGroup.addViewer(wdPlayer.playerId)
            // todo Забираем право говорить в plasmo
        }

        // РАССАДКА
        game.countries.forEach { country ->
            country.citizens.forEach { player ->

                val location = assignSeat(country, player)

                if (location != null) {
                    player.player.teleportAsync(location)
                    // todo mount
                }
            }
        }

        // ЦИКЛ ВЫСТУПЛЕНИЙ
        game.countries.forEachIndexed { idx, country ->
            inst.server.scheduler.runTaskLater(inst, Runnable {
                speakingCountry = country.type.ordinal
                nowSpeaking.clear()

                // Таймер 90 секунд на выступление страны
                val timer = BossBarTimer()
                timer.start(
                    totalSeconds = 90 / 10, // todo убрать / 10
                    isActive = { game.fsm.current is WDUNMeeting },
                    onFinish = {
                        country.citizens.forEach { wdPlayer ->
                            val location = occupiedSeats.entries.firstOrNull { it.value == wdPlayer.playerId }?.key
                            if (location != null) {
                                wdPlayer.player.teleportAsync(location.facingToward(sceneCenter))
                            }
                        }
                    }
                )

                game.gamePlayers.forEach { wdPlayer ->
                    val player = wdPlayer.player

                    timer.addPlayer(player)

                    if (wdPlayer.country == country) {
                        // todo перевод
                        player.showTitle(Title.title(
                            Component.text(""), Component.text("Ваша очередь!").color(NamedTextColor.BLUE),
                            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))
                        ))

                        // todo перевод
                        ActionBar.send(
                            player,
                            Component.text(
                                "нажмите F для выхода на сцену"
                            ).color(NamedTextColor.GREEN),
                            durationMs = 90 * 1000
                        )

                        // todo Даём право говорить в plasmo
                    } else {
                        // todo перевод
                        ActionBar.send(
                            player,
                            Component.text(
                                "сейчас выступает ${country.getFormattedName(player)}"
                            ).color(NamedTextColor.BLUE),
                            durationMs = 90 * 1000
                        )
                    }
                }
            }, idx * 90 * 20L / 10 + 20L) // todo убрать / 10
        }

        // ЗАВЕРШЕНИЕ СОВЕЩАНИЯ
        inst.server.scheduler.runTaskLater(inst, Runnable {
            if (
                game.round >= 5 ||
                game.ecology <= 0.0 ||
                game.countries.count { it.isAlive } == 1
            ) {
                game.fsm.transitionTo(WDFinishingState(game))
            } else {
                game.fsm.transitionTo(WDUNMeeting(game))
            }
        }, game.countries.size * 90 * 20L / 10 + 20L) // todo убрать / 10
    }

    override fun onExitState() { }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) {
        game.gameVisibilityGroup.addViewer(gamePlayer.playerId)

        val country = gamePlayer.country ?: return

        val location = assignSeat(country, gamePlayer)
        if (location != null) {
            gamePlayer.player.teleportAsync(location.facingToward(sceneCenter))
            // todo mount
        }
    }

    override fun onPlayerLeave(gamePlayer: WDPlayer) { }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false

    private val occupiedSeats: MutableMap<Location, UUID> = mutableMapOf()

    private fun Location.facingToward(target: Location): Location {
        val dx = target.x - x
        val dz = target.z - z
        val yaw = Math.toDegrees(atan2(-dx, dz)).toFloat()
        return Location(world, x, y, z, yaw, 0f)
    }

    private fun assignSeat(country: Country, player: WDPlayer): Location? {
        val locations = seats.getValue(country.type.ordinal)
        val location = locations.firstOrNull { it !in occupiedSeats } ?: return null

        occupiedSeats[location] = player.playerId
        return location.facingToward(sceneCenter)
    }

    companion object {
        val seats: Map<Int, List<Location>> by lazy {
            mapOf(
                0 to listOf(
                    Location(WDGame.world, 1018.5, 105.0, -5.5),
                    Location(WDGame.world, 1016.5, 105.0, -3.5),
                    Location(WDGame.world, 1014.5, 105.0, -2.5),
                    Location(WDGame.world, 1010.5, 105.0, 0.5),
                    Location(WDGame.world, 1008.5, 105.0, 0.5)
                ),
                1 to listOf(
                    Location(WDGame.world, 1018.5, 104.0, -7.5),
                    Location(WDGame.world, 1015.5, 104.0, -5.5),
                    Location(WDGame.world, 1013.5, 104.0, -4.5),
                    Location(WDGame.world, 1010.5, 104.0, -3.5),
                    Location(WDGame.world, 1007.5, 104.0, -2.5)
                ),
                2 to listOf(
                    Location(WDGame.world, 1016.5, 103.0, -8.5),
                    Location(WDGame.world, 1013.5, 103.0, -6.5),
                    Location(WDGame.world, 1010.5, 103.0, -5.5),
                    Location(WDGame.world, 1008.5, 103.0, -4.5),
                    Location(WDGame.world, 1006.5, 103.0, -4.5)
                ),
                3 to listOf(
                    Location(WDGame.world, 1015.5, 102.0, -10.5),
                    Location(WDGame.world, 1012.5, 102.0, -8.5),
                    Location(WDGame.world, 1010.5, 102.0, -7.5),
                    Location(WDGame.world, 1008.5, 102.0, -7.5),
                    Location(WDGame.world, 1005.5, 102.0, -6.5)
                ),
                4 to listOf(
                    Location(WDGame.world, 1013.5, 101.0, -11.5),
                    Location(WDGame.world, 1010.5, 101.0, -10.5),
                    Location(WDGame.world, 1008.5, 101.0, -9.5),
                    Location(WDGame.world, 1006.5, 101.0, -8.5),
                    Location(WDGame.world, 1004.5, 101.0, -8.5)
                ),
                5 to listOf(
                    Location(WDGame.world, 993.5, 105.0, 0.5),
                    Location(WDGame.world, 990.5, 105.0, -1.5),
                    Location(WDGame.world, 987.5, 105.0, -2.5),
                    Location(WDGame.world, 984.5, 105.0, -3.5),
                    Location(WDGame.world, 982.5, 105.0, -5.5)
                ),
                6 to listOf(
                    Location(WDGame.world, 992.5, 104.0, -2.5),
                    Location(WDGame.world, 989.5, 104.0, -3.5),
                    Location(WDGame.world, 986.5, 104.0, -4.5),
                    Location(WDGame.world, 984.5, 104.0, -5.5),
                    Location(WDGame.world, 982.5, 104.0, -7.5)
                ),
                7 to listOf(
                    Location(WDGame.world, 993.5, 103.0, -4.5),
                    Location(WDGame.world, 990.5, 103.0, -5.5),
                    Location(WDGame.world, 988.5, 103.0, -6.5),
                    Location(WDGame.world, 986.5, 103.0, -7.5),
                    Location(WDGame.world, 984.5, 103.0, -8.5)
                ),
                8 to listOf(
                    Location(WDGame.world, 995.5, 102.0, -6.5),
                    Location(WDGame.world, 992.5, 102.0, -7.5),
                    Location(WDGame.world, 989.5, 102.0, -8.5),
                    Location(WDGame.world, 987.5, 102.0, -9.5),
                    Location(WDGame.world, 985.5, 102.0, -11.5)
                ),
                9 to listOf(
                    Location(WDGame.world, 996.5, 101.0, -8.5),
                    Location(WDGame.world, 994.5, 101.0, -8.5),
                    Location(WDGame.world, 992.5, 101.0, -9.5),
                    Location(WDGame.world, 989.5, 101.0, -10.5),
                    Location(WDGame.world, 987.5, 101.0, -12.5)
                )
            )
        }

        val sceneTeleport by lazy {
            Location(WDGame.world, 1000.5, 100.0, 0.0, 180f, 0f)
        }

        val sceneCenter by lazy {
            Location(WDGame.world, 1000.5, 101.0, -16.5)
        }
    }
}