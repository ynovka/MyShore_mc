package ru.ynovka.myShore.game.tag.states

import ru.ynovka.myShore.game.tag.TagPlayerSetup.applyInProgressInventory
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.game.tag.maps.teleportPlayers
import ru.ynovka.myShore.text.actionBar.clearActionBar
import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import net.kyori.adventure.text.format.NamedTextColor
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.tag.TagPlayerRoles
import ru.ynovka.myShore.text.ComponentDecorator
import ru.ynovka.myShore.utils.Utils.clearTeams
import ru.ynovka.myShore.game.tag.TagPlayer
import ru.ynovka.myShore.game.tag.TagGame
import org.bukkit.potion.PotionEffectType
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GameWorldOld
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.utils.canMove
import net.kyori.adventure.title.Title
import org.bukkit.potion.PotionEffect
import org.bukkit.scoreboard.Team
import org.bukkit.GameMode
import java.time.Duration
import org.bukkit.Bukkit
import org.bukkit.Sound


// 5 сек перед началом (что бы у игроков загрузилась карта, они ознакомились со своими ролями)
class TagCountdown(game: TagGame) : GameState<TagPlayer, GameWorldOld, TagGame>(game) {



    override fun onEnterState() {
        val hunterId = game.gamePlayers.random().playerId

        game.gamePlayers.forEach { tagPlayer ->
            val isHunter = tagPlayer.playerId == hunterId

            val player = tagPlayer.player
            scheduler.schedule {
                player.applyInProgressInventory()
                player.clearTeams()

                player.addPotionEffect(glowingEffect)
                player.gameMode = GameMode.ADVENTURE
                player.canMove(false)

                if (isHunter) {
                    hunterTeam.addEntry(player.name)
                    tagPlayer.role = TagPlayerRoles.HUNTER

                    player.showTitle(Title.title(
                        Component.text(""),
                        Component.translatable("sub.title.myshore.tag.player_is_hunter"),
                        Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                    ))
                } else {
                    victimTeam.addEntry(player.name)
                    tagPlayer.role = TagPlayerRoles.VICTIM

                    player.showTitle(Title.title(
                        Component.text(""),
                        Component.translatable("sub.title.myshore.tag.player_is_runner"),
                        Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                    ))
                }
            }
                .entity(player)
                .once()
        }

        game.map.teleportPlayers(game)
        game.map.onGameStart(game)

        // обратный отсчёт
        ActionbarTimer.startCountdownTimer(
            time = 5,
            game = game,
            state = this,
            onCompletion = { tGame, _ ->
                tGame.gamePlayers.asPlayers().forEach { player ->
                    scheduler.schedule {
                        player.clearActionBar()
                        player.sendActionBar(
                            ComponentDecorator.addBackground(
                                Component.translatable("bar.myshore.tag.lets_run"),
                                player
                            )
                        )
                        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                    }.entity(player).once()
                }

                game.fsm.transitionTo(TagInProgressState(game))
            }
        )
    }

    override fun canPlayerJoin(gamePlayer: TagPlayer): Boolean = false


    private val scoreboard by lazy { Bukkit.getScoreboardManager().mainScoreboard }

    private val hunterTeam by lazy {
        (scoreboard.getTeam("tag_hunter") ?: scoreboard.registerNewTeam("tag_hunter"))
            .apply {
                color(NamedTextColor.RED)
                setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
            }
    }

    private val victimTeam by lazy {
        (scoreboard.getTeam("tag_victim") ?: scoreboard.registerNewTeam("tag_victim"))
            .apply {
                color(NamedTextColor.AQUA)
                setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
            }
    }

    private val glowingEffect = PotionEffect(PotionEffectType.GLOWING, -1, 0, false, false)
}