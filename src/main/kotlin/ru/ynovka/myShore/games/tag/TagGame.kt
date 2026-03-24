package ru.ynovka.myShore.games.tag

import ru.ynovka.myShore.games.tag.states.TagWaitingForPlayersState
import ru.ynovka.myShore.games.tag.states.TagInProgressState
import ru.ynovka.myShore.games.tag.states.TagFinishingState
import ru.ynovka.myShore.games.tag.states.TagPreparingState
import ru.ynovka.myShore.games.tag.states.TagVotingState
import ru.ynovka.myShore.games.tag.maps.TagMaps
import ru.ynovka.myShore.games.tag.maps.TagMap
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.text.clearActionBar
import ru.ynovka.myShore.games.GameFSM
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.utils.canMove
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import java.util.UUID


class TagGame : Game<TagPlayer>() {

    override val maxPlayers: Int = 8
    override val players: MutableList<TagPlayer> = mutableListOf()
    override val fsm = GameFSM(TagWaitingForPlayersState)

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

    override fun getOrCreatePlayer(player: Player): TagPlayer =
        players.firstOrNull { it.player.uniqueId == player.uniqueId }
            ?: TagPlayer(player)

    override fun handlePlayerJoin(player: TagPlayer) {
        map.onPlayerJoin(this, player.player)
    }

    override fun handlePlayerLeave(player: TagPlayer) {
        player.player.clearActivePotionEffects()
        player.player.canMove(true)
        player.player.clearActionBar()
        map.onPlayerLeave(this, player.player)

        when (fsm.current) {
            TagVotingState ->
                if (players.size <= 1) fsm.transitionTo(TagWaitingForPlayersState)

            TagPreparingState, TagInProgressState ->
                if (!hasVictims() || !hasHunter()) fsm.transitionTo(TagFinishingState)

            else -> Unit
        }
    }
}

// ---------- роли ----------

enum class TagPlayerRoles {
    UNDEFINED,
    SPECTATOR,
    SPECTATOR_VICTIM,
    VICTIM,
    HUNTER
}

// ---------- вспомогательные функции поиска игроков ----------

/** Найти TagPlayer по UUID; null если не в игре. */
fun TagGame.findPlayer(uuid: UUID): TagPlayer? =
    players.firstOrNull { it.player.uniqueId == uuid }

/** Найти TagPlayer по Bukkit Player. */
fun TagGame.findPlayer(player: Player): TagPlayer? =
    players.firstOrNull { it.player.uniqueId == player.uniqueId }

// ---------- extension-функции состояния ----------

fun TagGame.hasVictims(): Boolean = players.any { it.role == TagPlayerRoles.VICTIM }
fun TagGame.hasHunter(): Boolean  = players.any { it.role == TagPlayerRoles.HUNTER }

/**
 * Асинхронный телепорт игрока на позицию, соответствующую его роли на этой карте.
 * [onComplete] вызывается в main thread после успешного телепорта.
 */
fun TagMap.teleport(player: Player, game: TagGame, onComplete: () -> Unit = {}) {
    val role = game.findPlayer(player)?.role
        ?: if (game.fsm.current == TagInProgressState || game.fsm.current == TagPreparingState) {
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
                .firstOrNull { it.role == TagPlayerRoles.HUNTER }
                ?.player
            hunter?.location ?: victimSpawns.random().toLocation()
        }
    }

    player.teleportAsync(destination).thenAccept {
        Bukkit.getScheduler().runTask(inst, Runnable { onComplete() })
    }
}