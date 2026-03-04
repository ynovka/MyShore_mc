package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.asset.definition.Selector
import com.github.darksoulq.abyssallib.server.resource.asset.Texture
import com.github.darksoulq.abyssallib.server.resource.ResourcePack
import com.github.darksoulq.abyssallib.world.item.Item
import ru.ynovka.myShore.MyShore.Companion.PLUGIN_ID
import ru.ynovka.myShore.MyShore.Companion.inst


object TexturePack {
    val pack = ResourcePack(inst, PLUGIN_ID)
    val ns = pack.namespace(PLUGIN_ID)

    fun register() {
        GuiTextures.register(ns)

        pack.register(true)
    }

    fun createItemTexture(item: Item) {
        createItemDef(item.id.value())
    }

    fun createItemTexture(item: String) {
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