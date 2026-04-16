package ru.ynovka.myShore.games.worldDomination.states

import com.github.darksoulq.abyssallib.world.advancement.AdvancementFrame
import com.github.darksoulq.abyssallib.world.advancement.Toast
import ru.ynovka.myShore.games.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.games.worldDomination.WDPlayerRole
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDItems
import net.kyori.adventure.text.format.NamedTextColor
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.plasmo.PhoneCall
import ru.ynovka.myShore.games.GamePlayer
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.games.GameState
import org.bukkit.inventory.ItemStack
import org.bukkit.Material


/**
 * Этап переговоров и действий
 * Длится ровно 10 минут
 * В это время каждая страна может принять 1 другую страну на переговоры (5 минут)
 * И может отправить запрес на переговоры 1 другой страной (5 минут) (если страна откланила - не считается)
 * Параллельно с переговорами президент и вице президент могут распределять бюджет страны.
 */
class WDNegotiations(game: WDGame) : GameState<WDPlayer, WDGame>(game) {
    override fun onEnterState() {
        game.round += 1
        game.countries.forEach { it.onStartNewRound() }

        // Уведомление о начала новой стадии
        // todo перевод
        val toast = Toast.builder()
            .line1(Component.text("Началась новая стадия").color(NamedTextColor.GRAY))
            .line2(Component.text("<#87CEEB>> Переговоры"))
            .icon(ItemStack.of(Material.CLOCK))
            .frame(AdvancementFrame.GOAL)
            .build()
        game.gamePlayers.asPlayers().forEach { player ->
            toast.send(player)
        }

        // Телепорт игроков по странам
        game.countries.forEach { country ->
            country.citizens.asPlayers().forEach { player ->
                country.teleport(player)
            }
        }

        // Выдаём телефоны президентам
        game.gamePlayers
            .filter { it.role == WDPlayerRole.PRESIDENT }
            .map(GamePlayer::player)
            .forEach {
                it.inventory.setItem(7, WDItems.wdPhoneMenu.getStack(null))
            }

        inst.server.scheduler.runTaskLater(inst, Runnable {
            game.fsm.transitionTo(WDUNMeeting(game))
        }, 10 * 60 * 20L)
    }

    override fun onExitState() {
        // завершаем все звонки
        PhoneCall.endAllCalls(game.gamePlayers.map(GamePlayer::playerId))

        // Забираем телефоны
        game.gamePlayers
            .filter { it.role == WDPlayerRole.PRESIDENT }
            .map(GamePlayer::player)
            .forEach { it.inventory.clear(7) }
    }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) { }

    override fun onPlayerLeave(gamePlayer: WDPlayer) { }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false
}