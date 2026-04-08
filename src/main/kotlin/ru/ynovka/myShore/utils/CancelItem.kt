package ru.ynovka.myShore.utils

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.world.item.ItemBuilder
import com.github.darksoulq.abyssallib.world.item.Item
import net.kyori.adventure.key.Key
import org.bukkit.Material


fun cancelItem(id: Key, init: ItemBuilder.() -> Unit = {}): Item =
    ItemBuilder(id, Material.RABBIT_FOOT).apply {
        onUse      { _, _, _    -> ActionResult.CANCEL }
        onUseOn    { _          -> ActionResult.CANCEL }
        onClick    { _, _, _, _ -> ActionResult.CANCEL }
        onDrop     { _          -> ActionResult.CANCEL }
        onSwapHand { _, _       -> ActionResult.CANCEL }
        init()
    }.build()