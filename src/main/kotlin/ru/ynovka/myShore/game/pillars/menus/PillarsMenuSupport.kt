package ru.ynovka.myShore.game.pillars.menus

import com.github.darksoulq.abyssallib.server.event.context.gui.GuiClickContext
import com.github.darksoulq.abyssallib.world.gui.GuiBuilder
import com.github.darksoulq.abyssallib.world.gui.GuiManager
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.pillars.states.PillarsFinishing

object PillarsMenuSupport {
    private val optionSlots = listOf(10, 12, 14, 16, 20, 22, 24)

    fun optionSlot(index: Int) = optionSlots.getOrElse(index) { 10 + index }

    fun displayName(value: Enum<*>): String =
        value.name
            .lowercase()
            .split("_")
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }

    fun icon(
        material: Material,
        name: Component,
        vararg lore: Component,
        glint: Boolean = false
    ): ItemStack {
        val stack = ItemStack.of(material)
        stack.setData(DataComponentTypes.ITEM_NAME, name.noItalic())
        stack.setData(DataComponentTypes.ITEM_MODEL, Key.key("minecraft", material.name.lowercase()))
        if (lore.isNotEmpty()) {
            stack.setData(DataComponentTypes.LORE, ItemLore.lore(lore.map { it.noItalic() }))
        }
        if (glint) {
            stack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
        }
        return stack
    }

    fun optionIcon(material: Material, name: Component, selected: Boolean): ItemStack =
        icon(
            material,
            name,
            Component.text(if (selected) "Selected" else "Click to select", if (selected) NamedTextColor.GREEN else NamedTextColor.GRAY),
            glint = selected
        )

    fun backButton(builder: GuiBuilder, game: PillarsGame) {
        builder.set(
            SlotPosition.top(26),
            GuiButton.of(
                icon(
                    Material.ARROW,
                    Component.text("Back", NamedTextColor.YELLOW),
                    Component.text("Return to next round settings.", NamedTextColor.GRAY)
                )
            ) { ctx ->
                openIfOwner(ctx, game) { player ->
                    VotingMainMenu.open(player, game)
                }
            }
        )
    }

    fun openIfOwner(ctx: GuiClickContext, game: PillarsGame, block: (Player) -> Unit) {
        updateIfOwner(ctx, game, block)
    }

    fun updateIfOwner(ctx: GuiClickContext, game: PillarsGame, block: (Player) -> Unit) {
        val player = ctx.source() as? Player ?: return
        if (game.fsm.current !is PillarsFinishing || !game.canOwnerControl(player)) {
            player.sendMessage(Component.text("Only the event owner can change next round settings now.", NamedTextColor.RED))
            GuiManager.close(player)
            return
        }

        GuiManager.remove(ctx.view())
        block(player)
    }

    private fun Component.noItalic(): Component =
        decoration(TextDecoration.ITALIC, false)
}
