package ru.ynovka.myShore.games.worldDomination.states

import com.github.darksoulq.abyssallib.world.advancement.AdvancementFrame
import com.github.darksoulq.abyssallib.world.advancement.Toast
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.GameState


/**
 *
 * Этап знакомства игроков, длится ровно 1 минуту
 */
class WDIntroductionPlayers(game: WDGame) : GameState<WDPlayer, WDGame>(game) {
    /**
     * Отправляем сооющение в чат с членами страны
     */
    override fun onEnterState() {
        // todo перевод
        val toast = Toast.builder()
            .line1(Component.text("Началась новая стадия", NamedTextColor.GRAY))
            .line2(Component.text("Знакомство игроков"))
            .icon(ItemStack.of(Material.CLOCK))
            .frame(AdvancementFrame.GOAL)
            .build()

        game.gamePlayers.forEach { wdPlayer ->
            val player = wdPlayer.player

            // Телепортируем всех в их страны
            wdPlayer.country?.teleport(player)

            // Уведомление-достижение о начале стадии переговоров
            toast.send(player)

            // todo Таймер 1 минута в bossbar
        }

        // Отсчёт 1 минута, до перехода к следующему этапу
        inst.server.scheduler.runTaskLater(inst, Runnable {
            game.fsm.transitionTo(WDNegotiations(game))
        }, 60 * 20L / 20) // todo убрать `/ 20`
    }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) {
        gamePlayer.country?.teleport(gamePlayer.player)
    }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false
}