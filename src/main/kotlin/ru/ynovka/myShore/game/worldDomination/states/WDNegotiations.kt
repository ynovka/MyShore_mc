package ru.ynovka.myShore.game.worldDomination.states

import com.github.darksoulq.abyssallib.world.advancement.AdvancementFrame
import com.github.darksoulq.abyssallib.world.advancement.Toast
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.game.worldDomination.WDPlayerRole
import com.github.darksoulq.abyssallib.extension.closeGui
import ru.ynovka.myShore.game.worldDomination.WDPlayer
import ru.ynovka.myShore.game.worldDomination.WDItems
import net.kyori.adventure.text.format.NamedTextColor
import ru.ynovka.myShore.game.worldDomination.WDGame
import ru.ynovka.myShore.utils.BossBarTimer
import ru.ynovka.myShore.plasmo.PhoneCall
import ru.ynovka.myShore.game.GamePlayer
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorld
import org.bukkit.inventory.ItemStack
import org.bukkit.Material


/**
 * Этап переговоров и действий
 * Длится ровно 10 минут
 * В это время каждая страна может принять 1 другую страну на переговоры (5 минут)
 * И может отправить запрес на переговоры 1 другой страной (5 минут) (если страна откланила - не считается)
 * Параллельно с переговорами президент и вице президент могут распределять бюджет страны.
 */
class WDNegotiations(game: WDGame) : GameState<WDPlayer, GameWorld, WDGame>(game) {
    override fun onEnterState() {
        game.round += 1
        game.countries.forEach { it.onStartNewRound() }

        // Уведомление о начала новой стадии
        // todo перевод
        val toast = Toast.builder()
            .titlle(Component.text("Началась новая стадия", NamedTextColor.GRAY))
            .subtitle(Component.text("Переговоры", NamedTextColor.WHITE))
            .icon(ItemStack.of(Material.CLOCK))
            .frame(AdvancementFrame.GOAL)
            .build()

        // Таймер 10 минут, до перехода к следующему этапу
        val timer = BossBarTimer()
        timer.start(
            totalSeconds = 10 * 60 / 15, // todo убрать / 15
            isActive = { game.fsm.current is WDNegotiations },
            onFinish = {
                game.fsm.transitionTo(WDUNMeeting(game))
            }
        )

        game.gamePlayers.asPlayers().forEach { player ->
            toast.send(player)
            timer.addPlayer(player)
        }

        // Телепорт игроков по странам
        game.countries.forEach { country ->
            country.citizens.asPlayers().forEach { player ->
                country.teleport(player)
            }
        }

        // Выдаём телефоны, ноутбуки президентам
        game.gamePlayers
            .filter { it.role == WDPlayerRole.PRESIDENT }
            .asPlayers()
            .forEach {
                it.inventory.setItem(0, WDItems.wdLaptopMenu.getStack(null))
                it.inventory.setItem(7, WDItems.wdPhoneMenu.getStack(null))
            }
    }

    override fun onExitState() {
        // завершаем все звонки
        PhoneCall.endAllCalls(game.gamePlayers.map(GamePlayer::playerId))

        // Забираем телефоны
        game.gamePlayers
            .filter { it.role == WDPlayerRole.PRESIDENT }
            .asPlayers()
            .forEach {
                it.inventory.clear(0)
                it.inventory.clear(7)
            }

        game.gamePlayers.asPlayers().forEach { player ->
            player.closeGui()
        }

        game.countries.forEach { country ->
            country.resolvePendingBombardments()
        }
    }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) { }

    override fun onPlayerLeave(gamePlayer: WDPlayer) { }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false
}