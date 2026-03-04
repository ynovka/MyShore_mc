package ru.ynovka.myShore.utils

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.server.event.ClickType
import com.github.darksoulq.abyssallib.server.event.InventoryClickType
import com.github.darksoulq.abyssallib.server.event.context.item.UseContext
import com.github.darksoulq.abyssallib.world.item.Item
import com.github.darksoulq.abyssallib.world.item.ItemBuilder
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.PlayerInventory

abstract class CancelItem(id: Key) : Item(id, Material.RABBIT_FOOT) {
    override fun onDrop(player: Player?) = ActionResult.CANCEL
    override fun onSwapHand(player: Player, current: EquipmentSlot) = ActionResult.CANCEL
    override fun onUse(source: LivingEntity, hand: EquipmentSlot, type: ClickType) = ActionResult.CANCEL
    override fun onUseOn(ctx: UseContext) = ActionResult.CANCEL
    override fun onClick(
        player: Player?,
        slot: Int,
        inventory: PlayerInventory?,
        type: InventoryClickType?
    ) = ActionResult.CANCEL
}

/**
 * DSL-хелпер: создаёт предмет, у которого все интерактивные действия
 * по умолчанию отменены (CANCEL). Блок [init] может переопределить
 * только нужные хендлеры поверх дефолтов.
 *
 * Порядок важен: сначала выставляем cancel-дефолты, потом применяем [init],
 * который при необходимости перезаписывает нужный хендлер.
 */
fun cancelItem(id: Key, init: ItemBuilder.() -> Unit = {}): Item =
    ItemBuilder(id, Material.RABBIT_FOOT).apply {
        onUse      { _, _, _    -> ActionResult.CANCEL }
        onUseOn    { _          -> ActionResult.CANCEL }
        onClick    { _, _, _, _ -> ActionResult.CANCEL }
        onDrop     { _          -> ActionResult.CANCEL }
        onSwapHand { _, _       -> ActionResult.CANCEL }
        init()  // пользовательские хендлеры перезаписывают дефолты
    }.build()