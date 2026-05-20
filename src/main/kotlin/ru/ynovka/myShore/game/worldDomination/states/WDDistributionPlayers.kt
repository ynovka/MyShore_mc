package ru.ynovka.myShore.game.worldDomination.states

import ru.ynovka.myShore.game.worldDomination.entity.Country.Companion.getFormattedName
import com.github.darksoulq.abyssallib.world.advancement.AdvancementFrame
import ru.ynovka.myShore.game.worldDomination.entity.CountryType
import com.github.darksoulq.abyssallib.world.advancement.Toast
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.game.worldDomination.entity.Country
import ru.ynovka.myShore.game.worldDomination.WDPlayerRole
import com.github.darksoulq.abyssallib.extension.closeGui
import ru.ynovka.myShore.game.worldDomination.WDPlayer
import ru.ynovka.myShore.game.worldDomination.WDItems
import ru.ynovka.myShore.game.worldDomination.WDGame
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.utils.BossBarTimer
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GamePlayer
import ru.ynovka.myShore.plasmo.PhoneCall
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorldOld
import org.bukkit.inventory.ItemStack
import org.bukkit.entity.Player
import org.bukkit.Material
import kotlin.math.ceil
import java.util.UUID


/** Этап распределения игроков по странам, длится 3 минуты */
class WDDistributionPlayers(game: WDGame) : GameState<WDPlayer, GameWorldOld, WDGame>(game) {
    private val timer = BossBarTimer()

    companion object {
        private const val MIN_COUNTRIES = 6
        private const val MAX_COUNTRIES = 10
        private const val MAX_PLAYERS_IN_COUNTRY = 5
        private const val FULL_DISTRIBUTION_MIN_PLAYERS = MIN_COUNTRIES * 2
    }

    /**
     * Случайным образом определяем президентов случайных стран и телепортируем их в штаб-квартиры.
     * Остальные игроки остаются в лобби.
     * У президентов есть 3 минуты, чтобы выбрать своего вице-президента.
     * По истечении 3 минут странам без вице-президентов они будут назначены случайным образом.
     */
    override fun onEnterState() {
        val wdPlayers = game.gamePlayers.shuffled()
        val countriesCount = getTargetCountriesCount(wdPlayers.size)
        val countriesList = CountryType.entries.shuffled().take(countriesCount)

        val toast = Toast.builder()
            .titlle(Component.text("Началась новая стадия", NamedTextColor.GRAY))
            .subtitle(Component.text("Распределение", NamedTextColor.WHITE))
            .icon(ItemStack.of(Material.CLOCK))
            .frame(AdvancementFrame.GOAL)
            .build()

        timer.start(
            totalSeconds = 3 * 60 / 20, // todo убрать / 20
            isActive = { game.fsm.current is WDDistributionPlayers },
            onFinish = {
                game.fsm.transitionTo(WDIntroductionPlayers(game))
            }
        )

        wdPlayers.asPlayers().forEach { player ->
            player.inventory.setItem(8, WDItems.wdNotebook.getStack(player))
            toast.send(player)
            timer.addPlayer(player)
        }

        wdPlayers.take(countriesCount).zip(countriesList)
            .forEach { (president, type) ->
                createCountryWithPresident(
                    president = president,
                    type = type,
                    givePhone = true
                )
            }
    }

    override fun onExitState() {
        timer.stop()
        invites.clear()

        val playersCount = game.gamePlayers.size

        ensureCountriesWithPresidents(
            targetCount = getBaseCountriesCount(playersCount),
            givePhone = false
        )

        assignMissingVicePresidents()

        ensureCountriesWithPresidents(
            targetCount = getTargetCountriesCount(playersCount),
            givePhone = false
        )

        assignMissingVicePresidents()

        distributeRemainingCitizens()

        PhoneCall.endAllCalls(game.gamePlayers.map(GamePlayer::playerId))

        game.gamePlayers
            .filter { it.role == WDPlayerRole.PRESIDENT }
            .asPlayers()
            .forEach { it.inventory.clear(7) }

        game.gamePlayers.asPlayers().forEach { player ->
            player.closeGui()
        }
    }

    override fun onPlayerJoin(gamePlayer: WDPlayer) {
        val player = gamePlayer.player

        player.teleportAsync(WDGame.hubLoc)

        player.inventory.setItem(8, WDItems.wdNotebook.getStack(player))

        timer.addPlayer(player)

        val toast = Toast.builder()
            .titlle(Component.text("Текущая стадия", NamedTextColor.GRAY))
            .subtitle(Component.text("Распределение", NamedTextColor.WHITE))
            .icon(ItemStack.of(Material.CLOCK))
            .frame(AdvancementFrame.GOAL)
            .build()

        toast.send(player)

        ensureCountriesWithPresidents(
            targetCount = getTargetCountriesCount(game.gamePlayers.size),
            givePhone = true
        )
    }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) {
        val player = gamePlayer.player

        timer.addPlayer(player)
        player.inventory.setItem(8, WDItems.wdNotebook.getStack(player))

        if (gamePlayer.role == WDPlayerRole.PRESIDENT) {
            player.inventory.setItem(7, WDItems.wdPhoneMenu.getStack(player))
        }

        val country = gamePlayer.country
        if (country != null) {
            country.teleport(player)
            return
        }

        player.teleportAsync(WDGame.hubLoc)
    }

    private fun getBaseCountriesCount(playersCount: Int): Int {
        if (playersCount <= 0) return 0

        return if (playersCount < FULL_DISTRIBUTION_MIN_PLAYERS) {
            playersCount.coerceAtMost(MIN_COUNTRIES)
        } else {
            MIN_COUNTRIES
        }
    }

    /**
     * - 1 игрок  -> 1 страна
     * - 2 игрока -> 2 страны
     * - 12 игроков -> 6 стран
     * - 30 игроков -> 6 стран
     * - 31 игрок -> 7 стран
     * - 50 игроков -> 10 стран
     */
    private fun getTargetCountriesCount(playersCount: Int): Int {
        if (playersCount <= 0) return 0

        val byPlayersLimit = ceil(playersCount.toDouble() / MAX_PLAYERS_IN_COUNTRY).toInt()

        return maxOf(
            getBaseCountriesCount(playersCount),
            byPlayersLimit
        ).coerceAtMost(MAX_COUNTRIES)
    }

    private fun ensureCountriesWithPresidents(
        targetCount: Int,
        givePhone: Boolean
    ) {
        while (game.countries.size < targetCount) {
            val president = game.gamePlayers
                .filter { it.country == null }
                .shuffled()
                .firstOrNull()
                ?: return

            val type = getRandomUnusedCountryType() ?: return

            createCountryWithPresident(
                president = president,
                type = type,
                givePhone = givePhone
            )
        }
    }

    private fun createCountryWithPresident(
        president: WDPlayer,
        type: CountryType,
        givePhone: Boolean
    ): Country {
        val player = president.player

        val country = Country.create(game, president, type).also { country ->
            game.countries += country
            country.teleport(player)
        }

        if (givePhone) {
            player.inventory.setItem(7, WDItems.wdPhoneMenu.getStack(player))
        }

        val toast = Toast.builder()
            .titlle(Component.text("Вы президент страны", NamedTextColor.GRAY))
            .subtitle(country.getFormattedName(player))
            .icon(ItemStack.of(Material.DIAMOND_BLOCK))
            .frame(AdvancementFrame.GOAL)
            .build()

        toast.send(player)



        return country
    }

    private fun assignMissingVicePresidents() {
        val unassignedPlayers = game.gamePlayers
            .filter { it.country == null }
            .shuffled()
            .toMutableList()

        game.countries.shuffled().forEach { country ->
            if (country.vicePresident == null && unassignedPlayers.isNotEmpty()) {
                val newVice = unassignedPlayers.removeAt(0)

                country.setVicePresident(newVice)
                country.teleport(newVice.player)
            }
        }
    }

    private fun distributeRemainingCitizens() {
        val unassignedPlayers = game.gamePlayers
            .filter { it.country == null }
            .shuffled()

        unassignedPlayers.forEach { wdPlayer ->
            val country = game.countries
                .filter { country -> getPlayersCount(country) < MAX_PLAYERS_IN_COUNTRY }
                .shuffled()
                .minByOrNull { country -> getPlayersCount(country) }
                ?: return@forEach

            country.addCitizen(wdPlayer)
        }
    }

    private fun getPlayersCount(country: Country): Int {
        return game.gamePlayers.count { it.country == country }
    }

    private fun getRandomUnusedCountryType(): CountryType? {
        val usedTypes = game.countries.map { it.type }.toSet()

        return CountryType.entries
            .filter { it !in usedTypes }
            .shuffled()
            .firstOrNull()
    }

    private val invites: MutableSet<ViceInvite> = mutableSetOf()

    data class ViceInvite(
        val vice: UUID,
        val president: UUID,
        val expiryIn: Long
    )

    fun inviteVice(
        vice: Player,
        president: Player
    ) {
        val i = ViceInvite(
            vice.uniqueId,
            president.uniqueId,
            System.currentTimeMillis().plus(15_000)
        )

        invites.add(i)

        scheduler.schedule {
            if (invites.contains(i)) {
                invites.remove(i)
            }
        }
            .after(15 * 20L, Clock.TICKS)
            .once()

        val presidentFormatedName = game.getOrCreatePlayer(president.uniqueId).getFormattedName()

        vice.sendMessage(
            Component.text()
                .append(
                    Component.translatable(
                        "msg.myshore.wd.invite_vice_president",
                        presidentFormatedName
                    )
                )
                .appendNewline()
                .append(
                    Component.translatable("btn.myshore.agree")
                        .color(NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/wd accept_invite_vice ${president.name}"))
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.text("Нажмите, чтобы принять приглашение")
                                    .color(NamedTextColor.BLUE)
                            )
                        )
                )
                .append(
                    Component.text(" | ").color(NamedTextColor.WHITE)
                )
                .append(
                    Component.translatable("btn.myshore.disagree")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/wd deny_invite_vice ${president.name}"))
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.text("Нажмите, чтобы отклонить приглашение")
                                    .color(NamedTextColor.BLUE)
                            )
                        )
                )
        )
    }

    fun acceptInviteVice(
        viceId: UUID,
        president: Player,
        game: WDGame
    ) {
        val i = invites.firstOrNull {
            it.vice == viceId && it.president == president.uniqueId
        } ?: return

        invites.remove(i)

        val country = game.countries.firstOrNull {
            it.president.playerId == president.uniqueId
        } ?: return

        if (country.vicePresident != null) return

        val wdPlayer = game.getOrCreatePlayer(viceId)

        if (wdPlayer.country != null) return

        country.setVicePresident(wdPlayer)
        country.teleport(wdPlayer.player)

        invites.removeIf {
            it.vice == viceId || it.president == president.uniqueId
        }

        // TODO: Пишем в чат президенту сообщение "{игрок} принял приглашение"
    }

    fun denyInviteVice(
        vice: Player,
        president: Player
    ) {
        val i = invites.firstOrNull {
            it.vice == vice.uniqueId && it.president == president.uniqueId
        } ?: return

        invites.remove(i)

        // TODO: Пишем в чат президенту сообщение "{игрок} отклонил приглашение"
    }
}