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
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator

object VotingPillarMenu {
    fun open(player: Player, game: PillarsGame) {
        GuiManager.open(player, create(game))
    }

    fun create(game: PillarsGame) = gui(
        MenuType.GENERIC_9X3,
        Component.text("Choose pillars", NamedTextColor.DARK_AQUA)
    ) {
        PillarGenerator.entries.forEachIndexed { index, pillar ->
            set(
                SlotPosition.top(PillarsMenuSupport.optionSlot(index)),
                GuiButton.of(
                    PillarsMenuSupport.optionIcon(
                        material = pillar.material(),
                        name = Component.text(PillarsMenuSupport.displayName(pillar), NamedTextColor.GRAY),
                        selected = game.nextRoundPillar == pillar
                    )
                ) { ctx ->
                    PillarsMenuSupport.updateIfOwner(ctx, game) { player ->
                        game.nextRoundPillar = pillar
                        open(player, game)
                    }
                }
            )
        }

        PillarsMenuSupport.backButton(this, game)
    }

    private fun PillarGenerator.material() = when (this) {
        PillarGenerator.DEFAULT -> Material.BEDROCK
        PillarGenerator.VOID -> Material.OBSIDIAN
    }
}
