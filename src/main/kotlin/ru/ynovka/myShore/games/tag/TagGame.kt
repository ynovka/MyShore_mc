package ru.ynovka.myShore.games.tag

import ru.ynovka.myShore.games.tag.states.WaitingForPlayersState
import ru.ynovka.myShore.games.tag.states.InProgressState
import ru.ynovka.myShore.games.tag.states.FinishingState
import ru.ynovka.myShore.games.tag.states.PreparingState
import ru.ynovka.myShore.games.tag.states.VotingState
import ru.ynovka.myShore.games.tag.maps.TagGameMaps
import ru.ynovka.myShore.games.tag.maps.TagGameMap
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.utils.Utils.asPlayer
import ru.ynovka.myShore.utils.clearActionBar
import java.util.concurrent.CompletableFuture
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.games.GameId
import ru.ynovka.myShore.lobby.Lobby
import ru.ynovka.myShore.games.Game
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import ru.ynovka.myShore.games.tag.maps.impl.JungleMap
import java.util.UUID


class TagGame(val lobby: Lobby) : Game {

    override val gameId: GameId = GameId.TAG
    override val name: String = "Салочки"

    /** UUID → роль; синхронизирован с lobby.members */
    val players: MutableMap<UUID, TagPlayerRoles> =
        lobby.members.associateWith { TagPlayerRoles.UNDEFINED }.toMutableMap()

    val scheduler = inst.server.scheduler

    var map: TagGameMap = TagGameMaps.RANDOM.mapProvider()
    val mapVotes: MutableMap<UUID, TagGameMap> = mutableMapOf()

    /**
     * Максимальное время игры в секундах.
     * При изменении [remainingTime] смещается пропорционально, чтобы BossBar
     * отображал прогресс корректно.
     */
    var totalTime: Int = 40
        set(newValue) {
            if (newValue == 40) {
                remainingTime = 40
            } else {
                remainingTime += newValue - field
            }
            field = newValue
        }

    var remainingTime: Int = 40

    /** Публичный маркер текущего состояния (удобен для UI, логов, условий). */
    var state: TagGameStates = TagGameStates.WAITING_FOR_PLAYERS
        private set

    private var stateImpl: GameState = stateOf(state)

    init {
        stateImpl.onStateStart(this)
    }

    override fun join(player: Player) {
        players[player.uniqueId] = TagPlayerRoles.UNDEFINED
        stateImpl.onPlayerJoin(this, player)
    }

    override fun leave(player: Player) {
        players.remove(player.uniqueId) ?: return
        player.clearActivePotionEffects()
        player.canMove(true)
        player.clearActionBar()
        JungleMap.hideDartsFromPlayer(player)

        when (state) {
            TagGameStates.VOTING ->
                if (players.size <= 1) transitionTo(TagGameStates.WAITING_FOR_PLAYERS)

            TagGameStates.PREPARING, TagGameStates.IN_PROGRESS ->
                if (!hasVictims() || !hasHunter()) transitionTo(TagGameStates.FINISHING)

            else -> Unit
        }
    }

    fun transitionTo(newState: TagGameStates) {
        state = newState
        stateImpl = stateOf(newState)
        stateImpl.onStateStart(this)
    }

    private fun stateOf(state: TagGameStates): GameState = when (state) {
        TagGameStates.WAITING_FOR_PLAYERS -> WaitingForPlayersState
        TagGameStates.VOTING              -> VotingState
        TagGameStates.PREPARING           -> PreparingState
        TagGameStates.IN_PROGRESS         -> InProgressState
        TagGameStates.FINISHING           -> FinishingState
    }
}

// ---------- роли и состояния ----------

enum class TagPlayerRoles {
    UNDEFINED,
    SPECTATOR,
    SPECTATOR_VICTIM,
    VICTIM,
    HUNTER
}

enum class TagGameStates {
    WAITING_FOR_PLAYERS,  // безлимит — ждём минимум 2 игроков
    VOTING,               // 10 сек — голосование за карту
    PREPARING,            // 5 сек  — показываем роли, фриз
    IN_PROGRESS,          // 40–115 сек — геймплей
    FINISHING             // 5 сек  — итоги, сброс ролей
}

// ---------- extension-функции ----------

/**
 * Асинхронный телепорт игрока на позицию, соответствующую его роли на этой карте.
 * [onComplete] вызывается в main thread после успешного телепорта.
 */
fun TagGameMap.teleport(player: Player, game: TagGame, onComplete: () -> Unit = {}) {
    val role = game.players[player.uniqueId]
        ?: if (game.state == TagGameStates.IN_PROGRESS || game.state == TagGameStates.PREPARING) {
            TagPlayerRoles.SPECTATOR
        } else {
            TagPlayerRoles.UNDEFINED
        }

    val destination = when (role) {
        TagPlayerRoles.UNDEFINED,
        TagPlayerRoles.VICTIM -> victimSpawns
            .shuffled()
            .firstOrNull { it.toLocation().getNearbyPlayers(1.0).isEmpty() }
            ?.toLocation()
            ?: victimSpawns.random().toLocation()

        TagPlayerRoles.HUNTER -> hunterSpawn.toLocation()

        TagPlayerRoles.SPECTATOR,
        TagPlayerRoles.SPECTATOR_VICTIM -> {
            val hunter = game.players
                .filterValues { it == TagPlayerRoles.HUNTER }
                .keys.firstOrNull()
                ?.asPlayer()
            hunter?.location ?: victimSpawns.random().toLocation()
        }
    }

    player.teleportAsync(destination).thenAccept {
        Bukkit.getScheduler().runTask(inst, Runnable { onComplete() })
    }
}

fun TagGameMap.teleportPlayers(game: TagGame) {
    val players = game.players.keys.asPlayers()

    val victimPlayers = players.filter {
        val role = game.players[it.uniqueId]
        role == TagPlayerRoles.VICTIM || role == TagPlayerRoles.UNDEFINED
    }

    val hunterPlayers = players.filter {
        game.players[it.uniqueId] == TagPlayerRoles.HUNTER
    }

    val spectatorPlayers = players.filter {
        val role = game.players[it.uniqueId]
        role == TagPlayerRoles.SPECTATOR || role == TagPlayerRoles.SPECTATOR_VICTIM
    }

    val shuffledVictimSpawns = victimSpawns.shuffled().toMutableList()

    val teleports = mutableListOf<CompletableFuture<Boolean>>()

    // Victims
    victimPlayers.forEachIndexed { index, player ->
        val spawn = shuffledVictimSpawns
            .getOrNull(index)
            ?.toLocation()
            ?: victimSpawns.random().toLocation()

        teleports += player.teleportAsync(spawn)
    }

    // Hunters
    hunterPlayers.forEach { player ->
        teleports += player.teleportAsync(hunterSpawn.toLocation())
    }

    // Spectators
    val hunter = hunterPlayers.firstOrNull()

    spectatorPlayers.forEach { player ->
        val loc = hunter?.location ?: victimSpawns.random().toLocation()
        teleports += player.teleportAsync(loc)
    }

    CompletableFuture.allOf(*teleports.toTypedArray())

    if (game.map == JungleMap) JungleMap.spawnPoisonDarts(game)
}

fun TagGame.hasVictims(): Boolean = players.values.any { it == TagPlayerRoles.VICTIM }
fun TagGame.hasHunter(): Boolean = players.values.any { it == TagPlayerRoles.HUNTER }
