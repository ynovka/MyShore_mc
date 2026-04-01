package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.asset.Font
import ru.ynovka.myShore.texturepack.TexturePack.ns


object GuiTextures {
    var MENU_1x9_028: Font.TextureGlyph? = null
    var MENU_2x9_048_26: Font.TextureGlyph? = null
    var MENU_6x9: Font.TextureGlyph? = null

    fun register() {
        val fn: Font = ns.font("gui", false)

        MENU_1x9_028 = fn.glyph(ns.texture("gui/menu_1x9_028"), 130, 13)
        MENU_2x9_048_26 = fn.glyph(ns.texture("gui/menu_2x9_048_26"), 148, 13)
        MENU_6x9 = fn.glyph(ns.texture("gui/menu_6x9"), 220, 13)
    }
}