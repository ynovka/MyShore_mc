package ru.ynovka.myShore.games.worldDomination.states

import com.github.darksoulq.abyssallib.world.advancement.AdvancementFrame
import com.github.darksoulq.abyssallib.world.advancement.Toast
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDGame
import net.kyori.adventure.text.format.NamedTextColor
import ru.ynovka.myShore.utils.BossBarTimer
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.games.GameState
import org.bukkit.inventory.ItemStack
import org.bukkit.Material


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
            .line2(Component.text("Знакомство игроков", NamedTextColor.WHITE))
            .icon(ItemStack.of(Material.CLOCK))
            .frame(AdvancementFrame.GOAL)
            .build()

        // Таймер минута, до перехода к следующему этапу
        val timer = BossBarTimer()
        timer.start(
            totalSeconds = 60 / 12, // todo убрать / 12
            isActive = { game.fsm.current is WDIntroductionPlayers },
            onFinish = {
                game.fsm.transitionTo(WDNegotiations(game))
            }
        )

        game.gamePlayers.forEach { wdPlayer ->
            val player = wdPlayer.player

            // Телепортируем всех в их страны
            wdPlayer.country?.teleport(player)

            // Уведомление-достижение о начале стадии переговоров
            toast.send(player)

            timer.addPlayer(player)
        }
    }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) {
        gamePlayer.country?.teleport(gamePlayer.player)
    }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false
}