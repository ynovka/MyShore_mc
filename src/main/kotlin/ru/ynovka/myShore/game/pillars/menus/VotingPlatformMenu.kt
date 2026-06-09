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
import ru.ynovka.myShore.game.pillars.generators.platform.PlatformGenerator

object VotingPlatformMenu {
    fun open(player: Player, game: PillarsGame) {
        GuiManager.open(player, create(game))
    }

    fun create(game: PillarsGame) = gui(
        MenuType.GENERIC_9X3,
        Component.text("Choose platform", NamedTextColor.DARK_AQUA)
    ) {
        PlatformGenerator.entries.forEachIndexed { index, platform ->
            set(
                SlotPosition.top(PillarsMenuSupport.optionSlot(index)),
                GuiButton.of(
                    PillarsMenuSupport.optionIcon(
                        material = platform.material(),
                        name = Component.text(PillarsMenuSupport.displayName(platform), NamedTextColor.GREEN),
                        selected = game.nextRoundPlatform == platform
                    )
                ) { ctx ->
                    PillarsMenuSupport.updateIfOwner(ctx, game) { player ->
                        game.nextRoundPlatform = platform
                        open(player, game)
                    }
                }
            )
        }

        PillarsMenuSupport.backButton(this, game)
    }

    private fun PlatformGenerator.material() = when (this) {
        PlatformGenerator.NULL -> Material.BARRIER
        PlatformGenerator.GRASS_BLOCKS -> Material.GRASS_BLOCK
        PlatformGenerator.SLIME_AND_EMERALD_BLOCKS -> Material.SLIME_BLOCK
    }
}
