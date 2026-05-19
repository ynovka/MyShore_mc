package ru.ynovka.myShore.game.worldDomination

import ru.ynovka.myShore.game.worldDomination.WDGame.Companion.currentWDGame
import com.github.darksoulq.abyssallib.world.item.component.builtin.ItemModel
import com.github.darksoulq.abyssallib.world.item.component.builtin.ItemName
import ru.ynovka.myShore.game.worldDomination.states.WDDistributionPlayers
import ru.ynovka.myShore.game.worldDomination.menus.WDLaptopMainMenu
import ru.ynovka.myShore.game.worldDomination.states.WDNegotiations
import com.github.darksoulq.abyssallib.server.event.ActionResult
import ru.ynovka.myShore.game.worldDomination.menus.WDPhoneMenu
import com.github.darksoulq.abyssallib.extension.openGui
import com.github.darksoulq.abyssallib.world.item.item
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.MyShore.Companion.inst
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.cancelItem
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.Material

object WDItems {

    fun register() {
        TexturePack.createItemTexture(wdPhoneMenu)
        ITEMS.register("wd_phone_menu") { wdPhoneMenu }
        ITEMS.register("wd_laptop_menu") { wdLaptopMenu }
        ITEMS.register("wd_notebook") { wdNotebook }
        ITEMS.register("wd_invisible_item") { wdInvisibleItem }
    }

    val wdInvisibleItem = cancelItem(Key.key(inst, "wd_invisible_item")) {
        component(ItemModel(NamespacedKey.minecraft(Material.AIR.toString().lowercase())))
        component(ItemName(Component.empty()))
    }

    val wdNotebook = item(Key.key(inst, "wd_notebook"), Material.WRITABLE_BOOK) {
        component(ItemModel(NamespacedKey.minecraft(Material.WRITABLE_BOOK.toString().lowercase())))
        onClick { _, _, _, _ -> ActionResult.CANCEL }
        onSwapHand { _, _ -> ActionResult.CANCEL }
        tooltip { player ->
            line(Component.translatable("desc.myshore.wd_notebook.1"))
        }
    }

    val wdLaptopMenu = cancelItem(Key.key(inst, "wd_laptop_menu")) {
        component(ItemModel(NamespacedKey.minecraft(Material.IRON_BLOCK.toString().lowercase())))
        tooltip { player ->
            line(Component.translatable("desc.myshore.wd_laptop_menu.1"))
        }
        onUse { source, _, _ -> openLaptopMenu(source as Player) }
        onUseOn { ctx -> openLaptopMenu(ctx.source as Player) }
    }
    private fun openLaptopMenu(
        player: Player
    ): ActionResult {
        val game = player.uniqueId.currentWDGame() ?: return ActionResult.PASS
        if (game.fsm.current !is WDNegotiations) return ActionResult.PASS

        player.openGui(WDLaptopMainMenu.get(player))

        return ActionResult.CANCEL
    }

    val wdPhoneMenu = cancelItem(Key.key(inst, "wd_phone_menu")) {
        tooltip { player ->
            line(Component.translatable("desc.myshore.wd_phone_menu.1"))
        }
        onUse { source, _, _ -> openPhoneMenu(source as Player) }
        onUseOn { ctx -> openPhoneMenu(ctx.source as Player) }
    }
    private fun openPhoneMenu(
        player: Player
    ): ActionResult {
        val game = player.currentWDGame() ?: return ActionResult.PASS
        when (game.fsm.current) {
            is WDDistributionPlayers -> player.openGui(WDPhoneMenu.get(game, player.uniqueId, WDPlayerRole.UNDEFINED))
            is WDNegotiations -> player.openGui(WDPhoneMenu.get(game, player.uniqueId, WDPlayerRole.PRESIDENT))
        }
        return ActionResult.CANCEL
    }
}