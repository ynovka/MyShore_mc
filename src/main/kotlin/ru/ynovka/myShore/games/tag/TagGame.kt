package ru.ynovka.myShore.games.tag

import ru.ynovka.myShore.games.tag.states.TagWaitingForPlayersState
import ru.ynovka.myShore.games.tag.states.TagInProgressState
import ru.ynovka.myShore.games.tag.states.TagFinishingState
import ru.ynovka.myShore.games.tag.states.TagPreparingState
import ru.ynovka.myShore.games.tag.states.TagVotingState
import ru.ynovka.myShore.games.tag.maps.TagMaps
import ru.ynovka.myShore.games.tag.maps.TagMap
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.Utils.asPlayer
import ru.ynovka.myShore.text.clearActionBar
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.games.GameId
import ru.ynovka.myShore.lobby.Lobby
import ru.ynovka.myShore.games.Game
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import java.util.UUID


class TagGame(val lobby: Lobby) : Game {

    override val gameId: GameId = GameId.TAG
    override val name: String = "Салочки"

    /** UUID → роль; синхронизирован с lobby.members */
    val players: MutableMap<UUID, TagPlayerRoles> =
        lobby.members.associateWith { TagPlayerRoles.UNDEFINED }.toMutableMap()

    val scheduler = inst.server.scheduler

    var map: TagMap = TagMaps.RANDOM.mapProvider()
    val mapVotes: MutableMap<UUID, TagMap> = mutableMapOf()

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

    private var stateImpl: GameState<TagGame> = stateOf(state)

    init {
        stateImpl.onStateStart(this)
    }

    override fun join(player: Player) {
        players[player.uniqueId] = TagPlayerRoles.UNDEFINED
        stateImpl.onPlayerJoin(this, player)
        map.onPlayerJoin(this, player)
    }

    override fun leave(player: Player) {
        players.remove(player.uniqueId) ?: return
        player.clearActivePotionEffects()
        player.canMove(true)
        player.clearActionBar()
        map.onPlayerLeave(this, player)

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

    private fun stateOf(state: TagGameStates): GameState<TagGame> = when (state) {
        TagGameStates.WAITING_FOR_PLAYERS -> TagWaitingForPlayersState
        TagGameStates.VOTING              -> TagVotingState
        TagGameStates.PREPARING           -> TagPreparingState
        TagGameStates.IN_PROGRESS         -> TagInProgressState
        TagGameStates.FINISHING           -> TagFinishingState
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
fun TagMap.teleport(player: Player, game: TagGame, onComplete: () -> Unit = {}) {
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

fun TagGame.hasVictims(): Boolean = players.values.any { it == TagPlayerRoles.VICTIM }
fun TagGame.hasHunter(): Boolean = players.values.any { it == TagPlayerRoles.HUNTER }
