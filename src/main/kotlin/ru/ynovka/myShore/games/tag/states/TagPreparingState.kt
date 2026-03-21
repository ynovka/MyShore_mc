package ru.ynovka.myShore.games.tag.states

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import ru.ynovka.myShore.games.tag.TagPlayerSetup.applyInProgressInventory
import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.games.tag.maps.teleportPlayers
import ru.ynovka.myShore.text.sendPermanentActionBar
import net.kyori.adventure.text.format.NamedTextColor
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.games.tag.TagGameStates
import ru.ynovka.myShore.utils.Utils.clearTeams
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.utils.Utils.asPlayer
import ru.ynovka.myShore.text.clearActionBar
import ru.ynovka.myShore.games.tag.TagGame
import org.bukkit.potion.PotionEffectType
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.utils.canMove
import net.kyori.adventure.title.Title
import org.bukkit.potion.PotionEffect
import org.bukkit.scoreboard.Team
import org.bukkit.entity.Player
import org.bukkit.GameMode
import java.time.Duration
import org.bukkit.Bukkit
import org.bukkit.Sound
import ru.ynovka.myShore.text.ComponentDecorator
import java.util.UUID


// 5 сек перед началом (что бы у игроков загрузилась карта, они ознакомились со своими ролями)
object TagPreparingState : GameState<TagGame> {

    const val MAX_HISTORY = 10

    // История охотников — хранится между раундами (живёт пока жив объект)
    val hunterHistory: ArrayDeque<UUID> = ArrayDeque()
    val hunterCount: MutableMap<UUID, Int> = mutableMapOf()

    // Команды инициализируем лениво, чтобы не трогать Bukkit до его старта.
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

    override fun onStateStart(game: TagGame) {
        val hunterUuid = chooseHunter(game.lobby.members.toList())
        registerHunter(hunterUuid)

        game.lobby.members.forEach { uuid ->
            val player = uuid.asPlayer() ?: return@forEach
            val isHunter = uuid == hunterUuid

            player.applyInProgressInventory()
            player.clearTeams()

            if (isHunter) {
                hunterTeam.addEntry(player.name)
                player.showTitle(Title.title(
                    Component.text(""),
                    Component.translatable("sub.title.myshore.tag.player_is_hunter"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                ))
                game.players[uuid] = TagPlayerRoles.HUNTER
            } else {
                victimTeam.addEntry(player.name)
                player.showTitle(Title.title(
                    Component.text(""),
                    Component.translatable("sub.title.myshore.tag.player_is_runner"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                ))
                game.players[uuid] = TagPlayerRoles.VICTIM
            }

            player.addPotionEffect(glowingEffect)
            player.gameMode = GameMode.ADVENTURE

            player.canMove(false)
        }

        game.map.teleportPlayers(game)
        game.map.onGameStart(game)

        startCountdown(game)
    }

    override fun onPlayerJoin(game: TagGame, player: Player) {
        player.setupAsSpectator(game)
    }

    // ---------- выбор охотника ----------

    fun chooseHunter(players: List<UUID>): UUID {
        // Если последние 2 охотника — одно лицо, баним его на этот раунд
        val banned: Set<UUID> = if (hunterHistory.size >= 2 &&
            hunterHistory.last() == hunterHistory[hunterHistory.size - 2]
        ) {
            setOf(hunterHistory.last())
        } else {
            emptySet()
        }

        val candidates = players.filterNot { it in banned }.ifEmpty { players }

        // Взвешенный рандом: чем реже был охотником — тем выше шанс
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
            if (game.state != TagGameStates.PREPARING) return

            if (timeLeft > 0) {
                game.lobby.members.asPlayers().forEach { player ->
                    player.sendPermanentActionBar(
                        ComponentDecorator.addBackground(
                            Component.translatable(
                                "bar.myshore.tag.start_in",
                                Component.text(timeLeft)
                            ),
                            player
                        )
                    )
                    player.playSound(player.location, Sound.BLOCK_COPPER_BULB_TURN_ON, 0.5f, 2f)
                }

                game.scheduler.runTaskLater(inst, Runnable { tick(timeLeft - 1) }, 20L)
            } else {
                game.lobby.members.asPlayers().forEach { player ->
                    player.clearActionBar()
                    player.sendActionBar(
                        ComponentDecorator.addBackground(
                            Component.translatable("bar.myshore.tag.lets_run"),
                            player
                        )
                    )
                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                }

                game.transitionTo(TagGameStates.IN_PROGRESS)
            }
        }

        tick(seconds)
    }
}