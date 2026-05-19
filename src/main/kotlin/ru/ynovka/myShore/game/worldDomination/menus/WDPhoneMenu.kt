package ru.ynovka.myShore.game.worldDomination.menus

import ru.ynovka.myShore.game.worldDomination.entity.Country.Companion.getFormattedName
import ru.ynovka.myShore.game.worldDomination.states.WDDistributionPlayers
import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import ru.ynovka.myShore.game.worldDomination.states.WDNegotiations
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import ru.ynovka.myShore.game.worldDomination.WDPlayerRole
import com.github.darksoulq.abyssallib.extension.closeGui
import com.github.darksoulq.abyssallib.world.item.Item
import ru.ynovka.myShore.game.worldDomination.WDItems
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.NamedTextColor
import ru.ynovka.myShore.game.worldDomination.WDGame
import com.github.darksoulq.abyssallib.world.gui.*
import ru.ynovka.myShore.texturepack.GuiTextures
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import ru.ynovka.myShore.plasmo.PhoneCall
import net.kyori.adventure.text.Component
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import org.bukkit.Material
import org.bukkit.Bukkit
import java.util.*


@Suppress("UnstableApiUsage")
object WDPhoneMenu {
    fun get(
        game: WDGame,
        playerId: UUID,
        targetRole: WDPlayerRole
    ): Gui = gui(
        MenuType.GENERIC_9X6,
        Component.text().color(NamedTextColor.WHITE)
            .append(TextOffset.getOffset(-8))
            .append(GuiTextures.MENU_6x9)
            .append(TextOffset.getOffset(-170))
            .append(Component.translatable("menu.myshore.wd.phone"))
            .build(),
    ) { layer(WDPhoneLayer(game, playerId, targetRole)) }

    class WDPhoneLayer(
        private val game: WDGame,
        private val playerId: UUID,
        private val targetRole: WDPlayerRole
    ) : GuiLayer {
        override fun renderTo(view: GuiView) {
            val players = game.gamePlayers
                .filter { it.role == targetRole && playerId != it.playerId}
                .sortedBy { it.country?.president?.playerId }

            players.forEachIndexed { index, target ->
                /*val head = try { target.player.skull
                } catch (_: Exception) { ItemStack(Material.PLAYER_HEAD) }*/
                val head = ItemStack.of(Material.RABBIT_FOOT)

                view.inventoryView.setItem(SlotPosition.top(index).index, head)
                view.gui.elements[SlotPosition.top(index)] = GuiButton.of(head) { ctx ->
                    val player = ctx.view.inventoryView.player as Player
                    val playerFormattedName = game.getOrCreatePlayer(player.uniqueId).getFormattedName()

                    val head = ctx.currentItem ?: return@of
                    val targetPlayerUUID = (head.itemMeta as SkullMeta).owningPlayer?.uniqueId ?: return@of
                    val targetPlayer = Bukkit.getPlayer(targetPlayerUUID) ?: return@of
                    val targetFormattedName = game.getOrCreatePlayer(targetPlayer.uniqueId).getFormattedName()


                    // звоним выбранному игроку
                    PhoneCall.call(
                        player,
                        playerFormattedName,
                        targetPlayer,
                        targetFormattedName,
                        onEnd = {
                            clearInvisibleItems(targetPlayer)
                            targetPlayer.inventory.setItemInOffHand(ItemStack(Material.AIR))

                            clearInvisibleItems(player)
                            player.inventory.setItemInOffHand(ItemStack(Material.AIR))
                            player.inventory.setItem(7, WDItems.wdPhoneMenu.getStack(null))
                        },
                        onSuccessEnd = {
                            when (game.fsm.current) {
                                is WDDistributionPlayers -> sendDuggestInviteVice(player, targetPlayer, targetFormattedName)
                                is WDNegotiations -> player.inventory.setItem(7, ItemStack(Material.AIR))
                            }
                        }
                    )


                    val phone = WDItems.wdPhoneMenu.getStack(null)

                    // Перемещаем телефон звонящего в offhand
                    player.closeGui()
                    player.inventory.clear(7)
                    player.inventory.setItemInOffHand(phone)
                    fillHotbarInvisibleItems(player)

                    // Перемещаем телефон цели в offhand
                    targetPlayer.inventory.clear(7)
                    targetPlayer.inventory.setItemInOffHand(phone)
                    fillHotbarInvisibleItems(targetPlayer)
                }
            }
        }

        override fun cleanup(view: GuiView) {
            val size = view.top.size
            for (i in 0 until size) {
                view.gui.elements.remove(SlotPosition.top(i))
                view.top.setItem(i, null)
            }
        }
    }

    private fun sendDuggestInviteVice(
        player: Player,
        targetPlayer: Player,
        targetFormattedName: Component
    ) {
        player.sendMessage(
            Component.text()
                .append(
                    Component.translatable(
                        "msg.myshore.wd.would_invite_vice_president.1",
                        targetFormattedName
                    )
                )
                .appendNewline()
                .append(Component.translatable("msg.myshore.wd.would_invite_vice_president.2"))
                .appendNewline()
                .append(
                    Component.translatable("msg.myshore.wd.would_invite_vice_president.3")
                        .color(NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/wd invite_vice ${targetPlayer.name}"))
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.text("Нажмите, чтобы отправить приглашение").color(NamedTextColor.BLUE)
                            )
                        )
                )
                .build()
        )
    }

    private fun clearInvisibleItems(player: Player) {
        val inventory = player.inventory

        for (slot in 0..8) {
            val item = inventory.getItem(slot) ?: continue
            if (Item.resolve(item).id == WDItems.wdInvisibleItem.id) {
                inventory.clear(slot)
            }
        }
    }

    private fun fillHotbarInvisibleItems(player: Player) {
        val inventory = player.inventory

        for (slot in 0..8) {
            val item = inventory.getItem(slot)
            if (item == null) {
                inventory.setItem(slot, WDItems.wdInvisibleItem.getStack(null))
            }
        }
    }
}