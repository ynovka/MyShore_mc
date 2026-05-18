package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.worldDomination.entity.Country.Companion.getFormattedName
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import net.kyori.adventure.text.format.NamedTextColor
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.text.actionBar.ActionBar
import org.bukkit.persistence.PersistentDataType
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.utils.BossBarTimer
import ru.ynovka.myShore.plasmo.StageVoice
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.games.GamePlayer
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.GameWorld
import net.kyori.adventure.title.Title
import ru.ynovka.myShore.plasmo.Stage
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.NamespacedKey
import org.bukkit.Location
import ru.ynovka.myShore.games.worldDomination.WDWorld
import java.time.Duration
import kotlin.math.atan2
import java.util.UUID

/**
 * Этап совещания ООН.
 *
 * Длится от 9 до 15 минут: количество стран * 90 секунд.
 *
 * В этот период каждой стране даётся 90 секунд на выступление.
 *
 * Порядок стран для выступления:
 * - прилетело больше бомб;
 * - уровень развития;
 * - название по алфавиту.
 */
class WDUNMeeting(game: WDGame) : GameState<WDPlayer, GameWorld, WDGame>(game) {

    var speakingCountry: Int? = null
        private set

    var nowSpeaking: MutableSet<UUID> = mutableSetOf()
        private set

    private lateinit var stage: Stage

    private val occupiedSeats: MutableMap<UUID, SeatRef> = mutableMapOf()
    private val sittingPlayers: MutableMap<UUID, UUID> = mutableMapOf()

    private val seatCountryKey by lazy {
        NamespacedKey(inst, "wd_un_seat_country")
    }

    private val seatIndexKey by lazy {
        NamespacedKey(inst, "wd_un_seat_index")
    }

    override fun onEnterState() {
        createVoiceStage()
        addAllPlayersToVisibilityGroup()
        seatAllPlayers()
        startSpeakingCycle()
        scheduleMeetingFinish()
    }

    override fun onExitState() {
        speakingCountry = null
        nowSpeaking.clear()
    }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) {
        game.gameVisibilityGroup.addViewer(gamePlayer.playerId)

        val country = gamePlayer.country ?: return
        val seatLocation = assignSeat(country, gamePlayer) ?: return

        gamePlayer.player.teleportAsync(seatLocation).thenRun {
            scheduler.schedule {
                sitOnSeat(gamePlayer)
            }
                .sync()
                .once()
        }
    }

    override fun onPlayerLeave(gamePlayer: WDPlayer) {
        val player = gamePlayer.player
        unsit(player)
    }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false

    fun isSitting(playerId: UUID): Boolean {
        return sittingPlayers.containsKey(playerId)
    }

    fun ensureStillSitting(player: Player) {
        val standId = sittingPlayers[player.uniqueId] ?: return

        val stand = player.world.entities
            .filterIsInstance<ArmorStand>()
            .firstOrNull { it.uniqueId == standId && it.isValid }

        if (stand == null) {
            sittingPlayers.remove(player.uniqueId)
            return
        }

        if (!stand.passengers.contains(player)) {
            stand.addPassenger(player)
        }
    }

    fun forceUnsitAfterTeleport(player: Player) {
        unsit(player)
    }

    private fun createVoiceStage() {
        stage = StageVoice.createStage(
            game.gamePlayers.map(GamePlayer::playerId)
        )
    }

    private fun addAllPlayersToVisibilityGroup() {
        game.gamePlayers.forEach { wdPlayer ->
            game.gameVisibilityGroup.addViewer(wdPlayer.playerId)
        }
    }

    private fun seatAllPlayers() {
        game.countries.forEach { country ->
            country.citizens.forEach { wdPlayer ->
                val seatLocation = assignSeat(country, wdPlayer) ?: return@forEach

                wdPlayer.player.teleportAsync(seatLocation).thenRun {
                    scheduler.schedule {
                        sitOnSeat(wdPlayer)
                    }
                        .sync()
                        .once()
                }
            }
        }
    }

    private fun startSpeakingCycle() {
        game.countries.forEachIndexed { index, country ->
            scheduler.schedule {
                startCountrySpeech(country)
            }
                .sync()
                .after(getSpeechStartDelay(index), Clock.TICKS)
                .once()
        }
    }

    private fun startCountrySpeech(country: Country) {
        speakingCountry = country.type.ordinal
        nowSpeaking.clear()

        StageVoice.setSpeakers(
            stage = stage,
            speakerUuids = nowSpeaking
        )

        val timer = BossBarTimer()

        timer.start(
            totalSeconds = SPEECH_SECONDS / DEBUG_TIME_DIVIDER,
            isActive = { game.fsm.current is WDUNMeeting },
            onFinish = {
                returnCountryCitizensToSeats(country)
            }
        )

        game.gamePlayers.forEach { wdPlayer ->
            val player = wdPlayer.player

            timer.addPlayer(player)

            if (wdPlayer.country == country) {
                notifyCurrentCountryPlayer(player)
            } else {
                notifyListeningPlayer(player, country)
            }
        }
    }

    private fun notifyCurrentCountryPlayer(player: Player) {
        player.showTitle(
            Title.title(
                Component.text(""),
                Component.text("Ваша очередь!").color(NamedTextColor.BLUE),
                Title.Times.times(
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1),
                    Duration.ofMillis(500)
                )
            )
        )

        ActionBar.send(
            player = player,
            message = Component.text("нажмите F для выхода на сцену")
                .color(NamedTextColor.GREEN),
            durationMs = SPEECH_SECONDS * 1000L
        )
    }

    private fun notifyListeningPlayer(player: Player, country: Country) {
        ActionBar.send(
            player = player,
            message = Component.text("сейчас выступает ")
                .append(country.getFormattedName(player))
                .color(NamedTextColor.BLUE),
            durationMs = SPEECH_SECONDS * 1000L
        )
    }

    private fun returnCountryCitizensToSeats(country: Country) {
        country.citizens.forEach { wdPlayer ->
            val seatLocation = getOccupiedSeatLocation(wdPlayer.playerId) ?: return@forEach

            wdPlayer.player.teleportAsync(seatLocation).thenRun {
                scheduler.schedule {
                    sitOnSeat(wdPlayer)
                }
                    .sync()
                    .once()
            }
        }
    }

    private fun scheduleMeetingFinish() {
        scheduler.schedule {
            val nextState = if (shouldFinishGame()) {
                WDFinishingState(game)
            } else {
                WDNegotiations(game)
            }

            game.fsm.transitionTo(nextState)
        }
            .sync()
            .after(getMeetingFinishDelay(), Clock.TICKS)
            .once()
    }

    private fun shouldFinishGame(): Boolean {
        return game.round >= 5 ||
                game.ecology <= 0.0 ||
                game.countries.count { it.isAlive } == 1
    }

    private fun assignSeat(country: Country, wdPlayer: WDPlayer): Location? {
        val existingSeat = occupiedSeats[wdPlayer.playerId]

        if (existingSeat != null) {
            return getSeatLocation(existingSeat)
        }

        val countryOrdinal = country.type.ordinal
        val countrySeats = seats[countryOrdinal] ?: return null

        val usedIndexes = occupiedSeats.values
            .asSequence()
            .filter { it.countryOrdinal == countryOrdinal }
            .map { it.seatIndex }
            .toSet()

        val freeSeatIndex = countrySeats.indices
            .firstOrNull { it !in usedIndexes }
            ?: return null

        val seatRef = SeatRef(
            countryOrdinal = countryOrdinal,
            seatIndex = freeSeatIndex
        )

        occupiedSeats[wdPlayer.playerId] = seatRef

        return getSeatLocation(seatRef)
    }

    private fun getOccupiedSeatLocation(playerId: UUID): Location? {
        val seatRef = occupiedSeats[playerId] ?: return null
        return getSeatLocation(seatRef)
    }

    private fun getSeatLocation(seatRef: SeatRef): Location? {
        return seats[seatRef.countryOrdinal]
            ?.getOrNull(seatRef.seatIndex)
            ?.facingToward(sceneCenter)
    }

    private fun sitOnSeat(wdPlayer: WDPlayer): Boolean {
        val seatRef = occupiedSeats[wdPlayer.playerId] ?: return false
        val seatLocation = getSeatLocation(seatRef) ?: return false

        val stand = getOrCreateSeatStand(
            countryOrdinal = seatRef.countryOrdinal,
            seatIndex = seatRef.seatIndex,
            seatLocation = seatLocation
        )

        if (!stand.passengers.contains(wdPlayer.player)) {
            stand.addPassenger(wdPlayer.player)
        }

        sittingPlayers[wdPlayer.playerId] = stand.uniqueId
        return true
    }

    private fun unsit(player: Player) {
        val standId = sittingPlayers.remove(player.uniqueId) ?: return
        val stand = player.world.entities.firstOrNull { it.uniqueId == standId } ?: return

        if (stand.passengers.contains(player)) {
            stand.removePassenger(player)
        }
    }

    private fun getOrCreateSeatStand(
        countryOrdinal: Int,
        seatIndex: Int,
        seatLocation: Location
    ): ArmorStand {
        val world = seatLocation.world ?: error("Seat world is null")

        val existingStand = world.getNearbyEntities(seatLocation, 0.35, 0.35, 0.35)
            .asSequence()
            .filterIsInstance<ArmorStand>()
            .firstOrNull { stand ->
                stand.isValid &&
                        stand.scoreboardTags.contains(SEAT_TAG) &&
                        stand.persistentDataContainer.get(
                            seatCountryKey,
                            PersistentDataType.INTEGER
                        ) == countryOrdinal &&
                        stand.persistentDataContainer.get(
                            seatIndexKey,
                            PersistentDataType.INTEGER
                        ) == seatIndex
            }

        if (existingStand != null) {
            return existingStand
        }

        return world.spawn(seatLocation, ArmorStand::class.java) { stand ->
            stand.isInvisible = true
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.isSmall = true
            stand.setBasePlate(false)
            stand.isMarker = true
            stand.isCollidable = false
            stand.isPersistent = true
            stand.removeWhenFarAway = false

            stand.addScoreboardTag(SEAT_TAG)

            stand.persistentDataContainer.set(
                seatCountryKey,
                PersistentDataType.INTEGER,
                countryOrdinal
            )

            stand.persistentDataContainer.set(
                seatIndexKey,
                PersistentDataType.INTEGER,
                seatIndex
            )
        }
    }

    private fun Location.facingToward(target: Location): Location {
        val dx = target.x - x
        val dz = target.z - z
        val yaw = Math.toDegrees(atan2(-dx, dz)).toFloat()

        return Location(
            world,
            x,
            y,
            z,
            yaw,
            0f
        )
    }

    private fun getSpeechStartDelay(index: Int): Long {
        return index * SPEECH_SECONDS * TICKS_PER_SECOND / DEBUG_TIME_DIVIDER + TICKS_PER_SECOND
    }

    private fun getMeetingFinishDelay(): Long {
        return game.countries.size * SPEECH_SECONDS * TICKS_PER_SECOND / DEBUG_TIME_DIVIDER + TICKS_PER_SECOND
    }

    private data class SeatRef(
        val countryOrdinal: Int,
        val seatIndex: Int
    )

    companion object {
        private const val SEAT_TAG = "wd_un_meeting_seat"

        private const val SPEECH_SECONDS = 90
        private const val TICKS_PER_SECOND = 20L

        /**
         * TODO: поставить 1 перед релизом.
         */
        private const val DEBUG_TIME_DIVIDER = 10

        val seats: Map<Int, List<Location>> by lazy {
            mapOf(
                0 to listOf(
                    Location(WDWorld.world, 1018.5, 105.0, -5.5),
                    Location(WDWorld.world, 1016.5, 105.0, -3.5),
                    Location(WDWorld.world, 1014.5, 105.0, -2.5),
                    Location(WDWorld.world, 1010.5, 105.0, 0.5),
                    Location(WDWorld.world, 1008.5, 105.0, 0.5)
                ),
                1 to listOf(
                    Location(WDWorld.world, 1018.5, 104.0, -7.5),
                    Location(WDWorld.world, 1015.5, 104.0, -5.5),
                    Location(WDWorld.world, 1013.5, 104.0, -4.5),
                    Location(WDWorld.world, 1010.5, 104.0, -3.5),
                    Location(WDWorld.world, 1007.5, 104.0, -2.5)
                ),
                2 to listOf(
                    Location(WDWorld.world, 1016.5, 103.0, -8.5),
                    Location(WDWorld.world, 1013.5, 103.0, -6.5),
                    Location(WDWorld.world, 1010.5, 103.0, -5.5),
                    Location(WDWorld.world, 1008.5, 103.0, -4.5),
                    Location(WDWorld.world, 1006.5, 103.0, -4.5)
                ),
                3 to listOf(
                    Location(WDWorld.world, 1015.5, 102.0, -10.5),
                    Location(WDWorld.world, 1012.5, 102.0, -8.5),
                    Location(WDWorld.world, 1010.5, 102.0, -7.5),
                    Location(WDWorld.world, 1008.5, 102.0, -7.5),
                    Location(WDWorld.world, 1005.5, 102.0, -6.5)
                ),
                4 to listOf(
                    Location(WDWorld.world, 1013.5, 101.0, -11.5),
                    Location(WDWorld.world, 1010.5, 101.0, -10.5),
                    Location(WDWorld.world, 1008.5, 101.0, -9.5),
                    Location(WDWorld.world, 1006.5, 101.0, -8.5),
                    Location(WDWorld.world, 1004.5, 101.0, -8.5)
                ),
                5 to listOf(
                    Location(WDWorld.world, 993.5, 105.0, 0.5),
                    Location(WDWorld.world, 990.5, 105.0, -1.5),
                    Location(WDWorld.world, 987.5, 105.0, -2.5),
                    Location(WDWorld.world, 984.5, 105.0, -3.5),
                    Location(WDWorld.world, 982.5, 105.0, -5.5)
                ),
                6 to listOf(
                    Location(WDWorld.world, 992.5, 104.0, -2.5),
                    Location(WDWorld.world, 989.5, 104.0, -3.5),
                    Location(WDWorld.world, 986.5, 104.0, -4.5),
                    Location(WDWorld.world, 984.5, 104.0, -5.5),
                    Location(WDWorld.world, 982.5, 104.0, -7.5)
                ),
                7 to listOf(
                    Location(WDWorld.world, 993.5, 103.0, -4.5),
                    Location(WDWorld.world, 990.5, 103.0, -5.5),
                    Location(WDWorld.world, 988.5, 103.0, -6.5),
                    Location(WDWorld.world, 986.5, 103.0, -7.5),
                    Location(WDWorld.world, 984.5, 103.0, -8.5)
                ),
                8 to listOf(
                    Location(WDWorld.world, 995.5, 102.0, -6.5),
                    Location(WDWorld.world, 992.5, 102.0, -7.5),
                    Location(WDWorld.world, 989.5, 102.0, -8.5),
                    Location(WDWorld.world, 987.5, 102.0, -9.5),
                    Location(WDWorld.world, 985.5, 102.0, -11.5)
                ),
                9 to listOf(
                    Location(WDWorld.world, 996.5, 101.0, -8.5),
                    Location(WDWorld.world, 994.5, 101.0, -8.5),
                    Location(WDWorld.world, 992.5, 101.0, -9.5),
                    Location(WDWorld.world, 989.5, 101.0, -10.5),
                    Location(WDWorld.world, 987.5, 101.0, -12.5)
                )
            )
        }

        val sceneTeleport: Location by lazy {
            Location(WDWorld.world, 1000.5, 100.0, 0.0, 180f, 0f)
        }

        val sceneCenter: Location by lazy {
            Location(WDWorld.world, 1000.5, 101.0, -16.5)
        }
    }
}