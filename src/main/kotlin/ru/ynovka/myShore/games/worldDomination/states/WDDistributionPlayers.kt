package ru.ynovka.myShore.games.worldDomination.states

import com.github.darksoulq.abyssallib.world.advancement.AdvancementFrame
import com.github.darksoulq.abyssallib.world.advancement.Toast
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.worldDomination.entity.CountryType
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.GamePlayer
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDItems
import ru.ynovka.myShore.games.worldDomination.WDPlayer.Companion.asWDPlayer
import ru.ynovka.myShore.games.worldDomination.WDPlayerRole
import ru.ynovka.myShore.games.worldDomination.entity.Country.Companion.getFormattedName
import ru.ynovka.myShore.plasmo.PhoneCall
import ru.ynovka.myShore.utils.Utils.distribute
import java.util.UUID


/**
 * Этап распределния игроков по странам, длится 3 минуты
 * Снача определяется кол-во стран (кол-во игроков / 2, max 10)
 * Для тестов - минимум 2 игрока (2с по 1и)
 * Минимум 12 игроков (6с по 2и)
 * Максимум 50 игроков (10с по 5и)
 */

class WDDistributionPlayers(game: WDGame) : GameState<WDPlayer, WDGame>(game) {
    /**
     * Случайным образом определяем президентов случайных стран и телепортируем их в штаб-квартиры
     * Остальные игроки остаются в лобби
     * У президентов есть 3 минуты что бы выбрать своего вице-президента
     * По истечению 3-ёх минут странам без вице-президентов они будут назначенны случайным образом
     */
    override fun onEnterState() {
        // Случайное распределение стран и президентов
        val players = game.gamePlayers.shuffled()
        val countriesCount = (players.size / 2).coerceIn(2..10)
        val countriesList = CountryType.entries.shuffled().take(countriesCount)

        // todo перевод
        val toast = Toast.builder()
            .line1(Component.text("Началась новая стадия", NamedTextColor.GRAY))
            .line2(Component.text("Распределение"))
            .icon(ItemStack.of(Material.CLOCK))
            .frame(AdvancementFrame.GOAL)
            .build()

        players.map(GamePlayer::player).forEach {
            it.inventory.setItem(8, WDItems.wdNotebook.getStack(it))
            toast.send(it)
        }

        players.take(countriesCount).zip(countriesList)
            .forEach { (president, type) ->
                val pp = president.player
                // Создаём страну, телепортируем в неё презиента
                val country = Country.create(game, president, type).also { country ->
                    country.teleport(pp)
                }
                game.countries += country

                // Выдаём президенту телефон для звонков
                pp.inventory.setItem(7, WDItems.wdPhoneMenu.getStack(pp))

                // todo перевод
                val toast = Toast.builder()
                    .line1(Component.text("Вы президент страны", NamedTextColor.GRAY))
                    .line2(country.getFormattedName(pp))
                    .icon(ItemStack.of(Material.DIAMOND_BLOCK))
                    .frame(AdvancementFrame.GOAL)
                    .build()

                toast.send(pp)
            }

        // Отсчёт 3 минуты, до перехода к следующему этапу
        inst.server.scheduler.runTaskLater(inst, Runnable {
            game.fsm.transitionTo(WDIntroductionPlayers(game))
        }, 3 * 60 * 20L / 20) // todo убрать `/ 20`
    }


    override fun onExitState() {
        // Очищаем приглашения в вице-президенты
        invites.clear()

        // Распределяем оставшихся игроков по странам
        val unassignedPlayers = game.gamePlayers.filter { it.country == null }.shuffled().toMutableSet()
        game.countries.forEach { country ->
            if (country.vicePresident == null && unassignedPlayers.isNotEmpty()) {
                val newVice = unassignedPlayers.random()
                unassignedPlayers.remove(newVice)
                country.setVicePresident(newVice)
            }
        }
        if (unassignedPlayers.isNotEmpty()) {
            val chunkedPlayers = unassignedPlayers.distribute(game.countries.size)
            chunkedPlayers.zip(game.countries).forEach { (players, country) ->
                players.forEach { player ->
                    country.addCitizen(player)
                }
            }
        }

        // Завершаем все телефонные звонки
        PhoneCall.endAllCalls(game.gamePlayers.map(GamePlayer::playerId))

        // Забираем телефоны
        game.gamePlayers
            .filter { it.role == WDPlayerRole.PRESIDENT }
            .map(GamePlayer::player)
            .forEach { it.inventory.clear(7) }
    }

    override fun onPlayerJoin(gamePlayer: WDPlayer) {
        gamePlayer.player.teleportAsync(WDGame.hubLoc)
    }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) {
        val player = gamePlayer.player
        val country = gamePlayer.country
        if (country != null) {
            country.teleport(player)
            return
        }
        player.teleportAsync(WDGame.hubLoc)
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
        // пишем игроку TO сообщение с приглашением
        val i = ViceInvite(
            vice.uniqueId,
            president.uniqueId,
            System.currentTimeMillis().plus(15_000)
        )
        invites.add(i)

        Bukkit.getScheduler().runTaskLater(inst, Runnable {
            if (invites.contains(i)) {
                invites.remove(i)
            }
        }, 15 * 20L)

        val presidentFormatedName = president.asWDPlayer()?.getFormattedName() ?: Component.text(president.name)
        vice.sendMessage(
            Component.text()
                .append(Component.translatable(
                    "msg.myshore.wd.invite_vice_president", presidentFormatedName
                ))
                .appendNewline()
                .append(
                    Component.translatable("btn.myshore.agree")
                        .color(NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/wd accept_invite_vice ${president.name}"))
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.text("Нажмите, чтобы принять приглашение").color(NamedTextColor.BLUE)
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
                                Component.text("Нажмите, чтобы отклонить приглашение").color(NamedTextColor.BLUE)
                            )
                        )
                )
        )
    }

    fun acceptInviteVice(
        vice: Player,
        president: Player,
        game: WDGame
    ) {
        // игрок TO принял приглашение
        val i = invites.firstOrNull { it.vice == vice.uniqueId && it.president == president.uniqueId } ?: return
        invites.remove(i)

        val country = game.countries.firstOrNull { it.president.playerId == president } ?: return
        val wdPlayer = game.getOrCreatePlayer(vice)
        country.setVicePresident(wdPlayer)
        country.teleport(wdPlayer.player)

        // Пишем в чат президенту, сообщение "{игрок} принял приглашение"
    }

    fun denyInviteVice(
        vice: Player,
        president: Player
    ) {
        // игрок TO отклонил приглашение
        val i = invites.firstOrNull { it.vice == vice.uniqueId && it.president == president.uniqueId } ?: return
        invites.remove(i)

        // Пишем в чат президенту, сообщение "{игрок} отклонил приглашение"
    }
}