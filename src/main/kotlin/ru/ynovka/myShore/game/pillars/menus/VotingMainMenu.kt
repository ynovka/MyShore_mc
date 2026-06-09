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

object VotingMainMenu {
    fun open(player: Player, game: PillarsGame) {
        GuiManager.open(player, create(game))
    }

    fun create(game: PillarsGame) = gui(
        MenuType.GENERIC_9X3,
        Component.text("Next Pillars round", NamedTextColor.DARK_AQUA)
    ) {
        set(
            SlotPosition.top(10),
            GuiButton.of(
                PillarsMenuSupport.icon(
                    Material.NETHER_STAR,
                    Component.text("Game mode", NamedTextColor.GREEN),
                    Component.text(PillarsMenuSupport.displayName(game.nextRoundGameMode), NamedTextColor.GRAY)
                )
            ) { ctx ->
                PillarsMenuSupport.openIfOwner(ctx, game) { player ->
                    VotingGameModeMenu.open(player, game)
                }
            }
        )

        set(
            SlotPosition.top(12),
            GuiButton.of(
                PillarsMenuSupport.icon(
                    Material.HONEY_BLOCK,
                    Component.text("Allocator generator", NamedTextColor.GOLD),
                    Component.text(PillarsMenuSupport.displayName(game.nextRoundAllocator), NamedTextColor.GRAY)
                )
            ) { ctx ->
                PillarsMenuSupport.openIfOwner(ctx, game) { player ->
                    VotingAllocatorMenu.open(player, game)
                }
            }
        )

        set(
            SlotPosition.top(14),
            GuiButton.of(
                PillarsMenuSupport.icon(
                    Material.GRASS_BLOCK,
                    Component.text("Platform generator", NamedTextColor.GREEN),
                    Component.text(PillarsMenuSupport.displayName(game.nextRoundPlatform), NamedTextColor.GRAY)
                )
            ) { ctx ->
                PillarsMenuSupport.openIfOwner(ctx, game) { player ->
                    VotingPlatformMenu.open(player, game)
                }
            }
        )

        set(
            SlotPosition.top(16),
            GuiButton.of(
                PillarsMenuSupport.icon(
                    Material.BEDROCK,
                    Component.text("Pillar generator", NamedTextColor.GRAY),
                    Component.text(PillarsMenuSupport.displayName(game.nextRoundPillar), NamedTextColor.GRAY)
                )
            ) { ctx ->
                PillarsMenuSupport.openIfOwner(ctx, game) { player ->
                    VotingPillarMenu.open(player, game)
                }
            }
        )
    }
}
