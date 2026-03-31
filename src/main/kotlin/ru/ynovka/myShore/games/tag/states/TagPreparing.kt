package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.TagPlayerSetup.applyInProgressInventory
import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.games.tag.maps.teleportPlayers
import ru.ynovka.myShore.text.sendPermanentActionBar
import net.kyori.adventure.text.format.NamedTextColor
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagPlayer
import ru.ynovka.myShore.text.clearActionBar
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.utils.canMove
import org.bukkit.potion.PotionEffectType
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.potion.PotionEffect
import org.bukkit.scoreboard.Team
import org.bukkit.GameMode
import java.time.Duration
import org.bukkit.Bukkit
import org.bukkit.Sound
import ru.ynovka.myShore.text.ComponentDecorator
import ru.ynovka.myShore.utils.Utils.clearTeams
import java.util.UUID


// 5 сек перед началом (что бы у игроков загрузилась карта, они ознакомились со своими ролями)
object TagPreparing : GameState<TagPlayer> {

    const val MAX_HISTORY = 10

    val hunterHistory: ArrayDeque<UUID> = ArrayDeque()
    val hunterCount: MutableMap<UUID, Int> = mutableMapOf()

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

    override fun onEnter(game: Game<TagPlayer>) {
        val tagGame = game as TagGame
        val hunterUuid = chooseHunter(tagGame.gamePlayers.map { it.player.uniqueId })
        registerHunter(hunterUuid)

        tagGame.gamePlayers.forEach { tagPlayer ->
            val player = tagPlayer.player
            val isHunter = player.uniqueId == hunterUuid

            player.applyInProgressInventory()
            player.clearTeams()

            if (isHunter) {
                hunterTeam.addEntry(player.name)
                player.showTitle(Title.title(
                    Component.text(""),
                    Component.translatable("sub.title.myshore.tag.player_is_hunter"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                ))
                tagPlayer.role = TagPlayerRoles.HUNTER
            } else {
                victimTeam.addEntry(player.name)
                player.showTitle(Title.title(
                    Component.text(""),
                    Component.translatable("sub.title.myshore.tag.player_is_runner"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                ))
                tagPlayer.role = TagPlayerRoles.VICTIM
            }

            player.addPotionEffect(glowingEffect)
            player.gameMode = GameMode.ADVENTURE
            player.canMove(false)
        }

        tagGame.map.teleportPlayers(tagGame)
        tagGame.map.onGameStart(tagGame)

        startCountdown(tagGame)
    }

    override fun onPlayerJoin(game: Game<TagPlayer>, player: TagPlayer) {
        player.player.setupAsSpectator(game as TagGame)
    }

    // ---------- выбор охотника ----------

    fun chooseHunter(players: List<UUID>): UUID {
        val banned: Set<UUID> = if (hunterHistory.size >= 2 &&
            hunterHistory.last() == hunterHistory[hunterHistory.size - 2]
        ) {
            setOf(hunterHistory.last())
        } else {
            emptySet()
        }

        val candidates = players.filterNot { it in banned }.ifEmpty { players }

        val totalWeight = candidates.sumOf { 1.0 / (1 + hunterCount.getOrDefault(it, 0)) }
        var random = Math.random() * totalWeight

        for (uuid in candidates) {
            random -= 1.0 / (1 + hunterCount.getOrDefault(uuid, 0))
            if (random <= 0) return uuid
        }

        return candidates.random()
    }

    fun registerHunter(hunter: UUID) {
        hunterHistory.addLast(hunter)
        hunterCount[hunter] = hunterCount.getOrDefault(hunter, 0) + 1

        if (hunterHistory.size > MAX_HISTORY) {
            val removed = hunterHistory.removeFirst()
            val newCount = hunterCount.getOrDefault(removed, 1) - 1
            if (newCount <= 0) hunterCount.remove(removed) else hunterCount[removed] = newCount
        }
    }

    // ---------- обратный отсчёт ----------

    fun startCountdown(game: TagGame, seconds: Int = 5) {
        fun tick(timeLeft: Int) {
            if (game.fsm.current != TagPreparing) return

            if (timeLeft > 0) {
                game.gamePlayers.forEach { tagPlayer ->
                    tagPlayer.player.sendPermanentActionBar(
                        ComponentDecorator.addBackground(
                            Component.translatable(
                                "bar.myshore.tag.start_in",
                                Component.text(timeLeft)
                            ),
                            tagPlayer.player
                        )
                    )
                    tagPlayer.player.playSound(tagPlayer.player.location, Sound.BLOCK_COPPER_BULB_TURN_ON, 0.5f, 2f)
                }

                game.scheduler.runTaskLater(inst, Runnable { tick(timeLeft - 1) }, 20L)
            } else {
                game.gamePlayers.forEach { tagPlayer ->
                    tagPlayer.player.clearActionBar()
                    tagPlayer.player.sendActionBar(
                        ComponentDecorator.addBackground(
                            Component.translatable("bar.myshore.tag.lets_run"),
                            tagPlayer.player
                        )
                    )
                    tagPlayer.player.playSound(tagPlayer.player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                }

                game.fsm.transitionTo(TagInProgressState)
            }
        }

        tick(seconds)
    }
}