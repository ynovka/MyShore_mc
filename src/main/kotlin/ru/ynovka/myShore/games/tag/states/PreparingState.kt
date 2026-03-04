package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupAsSpectator
import ru.ynovka.myShore.utils.sendPermanentActionBar
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import ru.ynovka.myShore.games.tag.TagGameStates
import ru.ynovka.myShore.utils.Utils.clearTeams
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.PlayerRoles
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.utils.Utils.asPlayer
import ru.ynovka.myShore.utils.clearActionBar
import ru.ynovka.myShore.games.tag.TagGame
import org.bukkit.potion.PotionEffectType
import net.kyori.adventure.text.Component
import org.bukkit.potion.PotionEffect
import org.bukkit.entity.Player
import org.bukkit.GameMode
import org.bukkit.Bukkit
import org.bukkit.Sound
import ru.ynovka.myShore.games.tag.teleport
import ru.ynovka.myShore.utils.canMove
import java.util.UUID


// 5 сек перед началом (что бы у игроков загрузилась карта, они ознакомились со своими ролями)
object PreparingState : TagState {

    const val MAX_HISTORY = 10

    // История охотников — хранится между раундами (живёт пока жив объект)
    val hunterHistory: ArrayDeque<UUID> = ArrayDeque()
    val hunterCount: MutableMap<UUID, Int> = mutableMapOf()

    // Команды инициализируем лениво, чтобы не трогать Bukkit до его старта.
    private val scoreboard by lazy { Bukkit.getScoreboardManager().mainScoreboard }
    private val hunterTeam by lazy {
        (scoreboard.getTeam("tag_hunter") ?: scoreboard.registerNewTeam("tag_hunter"))
            .apply { color(NamedTextColor.RED) }
    }
    private val victimTeam by lazy {
        (scoreboard.getTeam("tag_victim") ?: scoreboard.registerNewTeam("tag_victim"))
            .apply { color(NamedTextColor.GREEN) }
    }

    private val glowingEffect = PotionEffect(PotionEffectType.GLOWING, -1, 0, false, false)

    override fun onStateStart(game: TagGame) {
        val hunterUuid = chooseHunter(game.lobby.members.toList())
        registerHunter(hunterUuid)

        game.lobby.members.forEach { uuid ->
            val player = uuid.asPlayer() ?: return@forEach
            val isHunter = uuid == hunterUuid

            player.inventory.clear()
            player.clearTeams()

            if (isHunter) {
                hunterTeam.addEntry(player.name)
                game.players[uuid] = PlayerRoles.HUNTER
            } else {
                victimTeam.addEntry(player.name)
                game.players[uuid] = PlayerRoles.VICTIM
            }

            player.addPotionEffect(glowingEffect)

            game.map.teleport(player, game) {
                player.gameMode = GameMode.ADVENTURE
            }

            // todo спавним кастомные предметы по позициям на карте
            player.canMove(false)
        }

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
                // todo перевод
                val msg = Component.text()
                    .append(Component.text("Старт через "))
                    .append(Component.text(timeLeft).decoration(TextDecoration.BOLD, true))
                    .append(Component.text(" секунд"))
                    .build()

                game.lobby.members.asPlayers().forEach { player ->
                    player.sendPermanentActionBar(msg)
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f)
                }

                game.scheduler.runTaskLater(inst, Runnable { tick(timeLeft - 1) }, 20L)
            } else {
                // todo перевод
                val msg = Component.text("ПОБЕЖАЛИ!")

                game.lobby.members.asPlayers().forEach { player ->
                    player.clearActionBar()
                    player.sendActionBar(msg)
                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                }

                game.transitionTo(TagGameStates.IN_PROGRESS)
            }
        }

        tick(seconds)
    }
}