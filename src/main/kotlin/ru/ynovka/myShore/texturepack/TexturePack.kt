package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.Namespace
import com.github.darksoulq.abyssallib.server.resource.ResourcePack
import com.github.darksoulq.abyssallib.server.resource.asset.Model
import com.github.darksoulq.abyssallib.server.resource.asset.Texture
import com.github.darksoulq.abyssallib.server.resource.asset.definition.Selector
import com.github.darksoulq.abyssallib.world.item.Item
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.MyShore.Companion.pluginId


object TexturePack {
    val pack = ResourcePack(inst, pluginId)
    val ns = pack.namespace(pluginId)

    fun register() {
        GuiTextures.register(ns)

        println("333333333333333 Registering")

        pack.register(true)
    }

    fun createItemTexture(item: Item) {
        println("createItemTexture for ${item.id.value()}")
        createItemDef(item.id.value())
    }

    fun createItemTexture(item: String) {
        println("createItemTexture_str for $item")
        createItemDef(item)
    }

    private fun createItemDef(name: String) {
        val tex: Texture? = ns.texture("item/$name")
        val model = ns.model(name, false)
        model.parent("minecraft:item/generated")
        model.texture("layer0", tex)

        val sel = Selector.Model(model)
        ns.itemDefinition(name, sel, false)
    }
}