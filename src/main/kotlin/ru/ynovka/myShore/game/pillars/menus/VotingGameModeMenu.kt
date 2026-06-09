package ru.ynovka.myShore.game.pillars.menus

import com.github.darksoulq.abyssallib.world.gui.GuiManager
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.pillars.gameMode.PillarsGameMode

object VotingGameModeMenu {
    fun open(player: Player, game: PillarsGame) {
        GuiManager.open(player, create(game))
    }

    fun create(game: PillarsGame) = gui(
        MenuType.GENERIC_9X3,
        Component.text("Choose game mode", NamedTextColor.DARK_AQUA)
    ) {
        PillarsGameMode.entries.forEachIndexed { index, mode ->
            set(
                SlotPosition.top(PillarsMenuSupport.optionSlot(index)),
                GuiButton.of(
                    PillarsMenuSupport.optionIcon(
                        material = mode.material(),
                        name = Component.text(PillarsMenuSupport.displayName(mode), NamedTextColor.GREEN),
                        selected = game.nextRoundGameMode == mode
                    )
                ) { ctx ->
                    PillarsMenuSupport.updateIfOwner(ctx, game) { player ->
                        game.nextRoundGameMode = mode
                        open(player, game)
                    }
                }
            )
        }

        PillarsMenuSupport.backButton(this, game)
    }

    private fun PillarsGameMode.material() = when (this) {
        PillarsGameMode.NULL -> Material.BARRIER
        PillarsGameMode.HOTBAR_ITEMS -> Material.CHEST
        PillarsGameMode.LAVA_RUSH -> Material.LAVA_BUCKET
    }
}
