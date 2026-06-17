package ru.ynovka.myShore.game.gameUtils

import com.github.darksoulq.abyssallib.world.gui.GuiManager
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.meta.SkullMeta
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.Game


object SpectatorTeleportMenu {
    private const val MAX_TARGETS = 54

    fun open(player: Player, game: Game<*, *>) {
        GuiManager.open(player, create(game))
    }

    private fun create(game: Game<*, *>) = gui(
        MenuType.GENERIC_9X6,
        Component.text("Teleport to player", NamedTextColor.DARK_AQUA)
    ) {
        val targets = game.activePlayers
            .mapNotNull { it.asPlayer() }
            .sortedBy { it.name.lowercase() }
            .take(MAX_TARGETS)

        if (targets.isEmpty()) {
            set(
                SlotPosition.top(22),
                GuiButton.of(
                    icon(
                        Material.BARRIER,
                        Component.text("No active players", NamedTextColor.RED),
                        Component.text("There is nobody to teleport to right now.", NamedTextColor.GRAY)
                    )
                ) {}
            )
            return@gui
        }

        targets.forEachIndexed { index, target ->
            set(
                SlotPosition.top(index),
                GuiButton.of(playerHead(target)) { ctx ->
                    val spectator = ctx.source() as? Player ?: return@of
                    teleportToActivePlayer(spectator, game, target.uniqueId)
                }
            )
        }
    }

    private fun teleportToActivePlayer(
        spectator: Player,
        game: Game<*, *>,
        targetId: java.util.UUID
    ) {
        if (!game.hasSpectator(spectator.uniqueId)) {
            GuiManager.close(spectator)
            return
        }

        if (game.activePlayers.none { it.playerId == targetId }) {
            spectator.sendMessage(Component.text("That player is no longer active.", NamedTextColor.RED))
            GuiManager.close(spectator)
            return
        }

        val target = Bukkit.getPlayer(targetId)
        if (target == null) {
            spectator.sendMessage(Component.text("That player is offline.", NamedTextColor.RED))
            GuiManager.close(spectator)
            return
        }

        scheduler.schedule {
            val targetLocation = target.location.clone()

            scheduler.schedule {
                spectator.teleportAsync(targetLocation)
                GuiManager.close(spectator)
            }.entity(spectator).once()
        }.entity(target).once()
    }

    private fun playerHead(player: Player): ItemStack {
        val stack = ItemStack.of(Material.PLAYER_HEAD)
        val meta = stack.itemMeta as? SkullMeta

        if (meta != null) {
            meta.owningPlayer = Bukkit.getOfflinePlayer(player.uniqueId)
            stack.itemMeta = meta
        }

        stack.setData(
            DataComponentTypes.ITEM_NAME,
            Component.text(player.name, NamedTextColor.YELLOW).noItalic()
        )
        stack.setData(
            DataComponentTypes.LORE,
            ItemLore.lore(
                listOf(Component.text("Click to teleport.", NamedTextColor.GRAY).noItalic())
            )
        )

        return stack
    }

    private fun icon(
        material: Material,
        name: Component,
        vararg lore: Component
    ): ItemStack {
        val stack = ItemStack.of(material)

        stack.setData(DataComponentTypes.ITEM_NAME, name.noItalic())
        if (lore.isNotEmpty()) {
            stack.setData(DataComponentTypes.LORE, ItemLore.lore(lore.map { it.noItalic() }))
        }

        return stack
    }

    private fun Component.noItalic(): Component =
        decoration(TextDecoration.ITALIC, false)
}
