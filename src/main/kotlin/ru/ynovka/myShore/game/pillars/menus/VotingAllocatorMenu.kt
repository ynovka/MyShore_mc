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
import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator

object VotingAllocatorMenu {
    fun open(player: Player, game: PillarsGame) {
        GuiManager.open(player, create(game))
    }

    fun create(game: PillarsGame) = gui(
        MenuType.GENERIC_9X3,
        Component.text("Choose allocator", NamedTextColor.DARK_AQUA)
    ) {
        AllocatorGenerator.entries.forEachIndexed { index, allocator ->
            set(
                SlotPosition.top(PillarsMenuSupport.optionSlot(index)),
                GuiButton.of(
                    PillarsMenuSupport.optionIcon(
                        material = allocator.material(),
                        name = Component.text(PillarsMenuSupport.displayName(allocator), NamedTextColor.GOLD),
                        selected = game.nextRoundAllocator == allocator
                    )
                ) { ctx ->
                    PillarsMenuSupport.updateIfOwner(ctx, game) { player ->
                        game.nextRoundAllocator = allocator
                        open(player, game)
                    }
                }
            )
        }

        PillarsMenuSupport.backButton(this, game)
    }

    private fun AllocatorGenerator.material() = when (this) {
        AllocatorGenerator.HONEY -> Material.HONEY_BLOCK
        AllocatorGenerator.RING -> Material.IRON_BARS
    }
}
