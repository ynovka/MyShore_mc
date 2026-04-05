package ru.ynovka.myShore.games.tag

import ru.ynovka.myShore.games.tag.states.TagWaitingForPlayers
import ru.ynovka.myShore.games.tag.states.TagInProgressState
import ru.ynovka.myShore.games.tag.states.TagFinishing
import ru.ynovka.myShore.games.tag.states.TagPreparing
import ru.ynovka.myShore.games.tag.states.TagVoting
import ru.ynovka.myShore.games.tag.maps.TagMaps
import ru.ynovka.myShore.games.tag.maps.TagMap
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.text.clearActionBar
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.utils.canMove
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import ru.ynovka.myShore.games.worldDomination.states.WDWaitingForPlayers
import java.util.UUID


class TagGame : Game<TagPlayer>() {

    override val initialState = TagWaitingForPlayers(this)
    override val maxPlayers: Int = 8
    override val gamePlayers: MutableSet<TagPlayer> = mutableSetOf()

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
        gamePlayers.firstOrNull { it.player.uniqueId == player.uniqueId }
            ?: TagPlayer(player.uniqueId)

    override fun handlePlayerJoin(player: TagPlayer) {
        map.onPlayerJoin(this, player.player)
    }

    override fun handlePlayerLeave(player: TagPlayer) {
        player.player.clearActivePotionEffects()
        player.player.canMove(true)
        player.player.clearActionBar()
        map.onPlayerLeave(this, player.player)

        when (fsm.current) {
            is TagVoting ->
                if (gamePlayers.size <= 1) fsm.transitionTo(TagWaitingForPlayers(this))

            is TagPreparing, is TagInProgressState ->
                if (!hasVictims() || !hasHunter()) fsm.transitionTo(TagFinishing(this))

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
    gamePlayers.firstOrNull { it.player.uniqueId == uuid }

/** Найти TagPlayer по Bukkit Player. */
fun TagGame.findPlayer(player: Player): TagPlayer? =
    gamePlayers.firstOrNull { it.player.uniqueId == player.uniqueId }

// ---------- extension-функции состояния ----------

fun TagGame.hasVictims(): Boolean = gamePlayers.any { it.role == TagPlayerRoles.VICTIM }
fun TagGame.hasHunter(): Boolean  = gamePlayers.any { it.role == TagPlayerRoles.HUNTER }

/**
 * Асинхронный телепорт игрока на позицию, соответствующую его роли на этой карте.
 * [onComplete] вызывается в main thread после успешного телепорта.
 */
fun TagMap.teleport(player: Player, game: TagGame, onComplete: () -> Unit = {}) {
    val role = game.findPlayer(player)?.role
        ?: if (game.fsm.current is TagInProgressState || game.fsm.current is TagPreparing) {
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
            val hunter = game.gamePlayers
                .firstOrNull { it.role == TagPlayerRoles.HUNTER }
                ?.player
            hunter?.location ?: victimSpawns.random().toLocation()
        }
    }

    player.teleportAsync(destination).thenAccept {
        Bukkit.getScheduler().runTask(inst, Runnable { onComplete() })
    }
}