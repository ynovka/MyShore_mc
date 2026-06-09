package ru.ynovka.myShore.game.pillars

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.server.event.ClickType
import com.github.darksoulq.abyssallib.world.item.component.builtin.ItemModel
import com.github.darksoulq.abyssallib.world.item.component.builtin.ItemName
import com.github.darksoulq.abyssallib.world.item.component.builtin.MaxStackSize
import com.github.darksoulq.abyssallib.world.item.item
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.MyShore.Companion.PLUGIN_ID
import ru.ynovka.myShore.game.pillars.PillarsGame.Companion.currentPillarsGame
import ru.ynovka.myShore.game.pillars.menus.VotingMainMenu
import ru.ynovka.myShore.game.pillars.states.PillarsFinishing

object PillarsItems {
    fun register() {
        ITEMS.register("pillars_next_round") { nextRound }
        ITEMS.register("pillars_round_settings") { roundSettings }
    }

    val nextRound = item(Key.key(PLUGIN_ID, "pillars_next_round"), Material.ENDER_EYE) {
        component(ItemName(Component.text("Start next round", NamedTextColor.GREEN)))
        component(ItemModel(Key.key("minecraft", "ender_eye")))
        component(MaxStackSize(1))
        tooltip { _: Player? ->
            line(Component.text("Right click to start the next Pillars round.", NamedTextColor.GRAY))
        }
        onClick { player, _, _, _ ->
            ActionResult.CANCEL
        }
        onUse { entity, _, _ ->
            startNextRound(entity as Player)
            ActionResult.CANCEL
        }
        onUseOn { ctx ->
            startNextRound(ctx.source as Player)
            ActionResult.CANCEL
        }
        onSwapHand { _, _ -> ActionResult.CANCEL }
        onDrop { _ -> ActionResult.CANCEL }
    }
    
    val roundSettings = item(Key.key(PLUGIN_ID, "pillars_round_settings"), Material.COMPARATOR) {
        component(ItemName(Component.text("Round settings", NamedTextColor.AQUA)))
        component(ItemModel(Key.key("minecraft", "comparator")))
        component(MaxStackSize(1))
        tooltip { _: Player? ->
            line(Component.text("Right click to configure the next Pillars round.", NamedTextColor.GRAY))
        }
        onClick { player, _, _, _ ->
            ActionResult.CANCEL
        }
        onUse { entity, _, _ ->
            openSettings(entity as Player)
            ActionResult.CANCEL
        }
        onUseOn { ctx ->
            openSettings(ctx.source as Player)
            ActionResult.CANCEL
        }
        onSwapHand { _, _ -> ActionResult.CANCEL }
        onDrop { _ -> ActionResult.CANCEL }
    }

    private fun startNextRound(player: Player) {
        val game = player.uniqueId.currentPillarsGame()
        if (game == null || game.fsm.current !is PillarsFinishing || !game.canOwnerControl(player)) {
            player.sendMessage(Component.text("Only the event owner can start the next round now.", NamedTextColor.RED))
            return
        }

        game.startNextRound()
    }

    private fun openSettings(player: Player) {
        val game = player.uniqueId.currentPillarsGame()
        if (game == null || game.fsm.current !is PillarsFinishing || !game.canOwnerControl(player)) {
            player.sendMessage(Component.text("Only the event owner can change next round settings now.", NamedTextColor.RED))
            return
        }

        VotingMainMenu.open(player, game)
    }
}
