package ru.ynovka.myShore.hub.menus

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.texturepack.GuiTextures
import net.kyori.adventure.text.Component
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.tag.TagGame
import ru.ynovka.myShore.game.worldDomination.WDGame
import ru.ynovka.myShore.hub.HubItems.playPillarsItem
import ru.ynovka.myShore.hub.HubItems.playTagItem
import ru.ynovka.myShore.hub.HubItems.playWDItem


object PlayMenu {
    fun get(): Gui = gui(
        MenuType.GENERIC_9X2,
        Component.text().color(NamedTextColor.WHITE)
            .append(TextOffset.getOffset(-8))
            .append(GuiTextures.MENU_HUB_PLAY)
            .append(TextOffset.getOffset(-170))
            .append(Component.translatable("menu.myshore.minigames"))
            .build(),
    ) {
        set(
            SlotPosition.top(0),
            GuiButton.of(playTagItem.getStack(null)) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                GameManager.join(player, ::TagGame)
            }
        )
        set(
            SlotPosition.top(4),
            GuiButton.of(playPillarsItem.getStack(null)) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                PillarsGame.hubWorld.teleportToSpawn(player)
                // todo GameManager.join(player, ::PillarsGame)
                // нужно вынести на NPS хабе / меню
            }
        )
        set(
            SlotPosition.top(11),
            GuiButton.of(playWDItem.getStack(null)) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                GameManager.join(player, ::WDGame)
            }
        )
    }
}
