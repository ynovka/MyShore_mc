package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.Namespace
import com.github.darksoulq.abyssallib.server.resource.asset.Font

object GuiTextures {
    var GENERIC_9X2_PAGE_MENU: Font.TextureGlyph? = null

    fun register(ns: Namespace) {
        val fn: Font = ns.font("gui", false)
        GENERIC_9X2_PAGE_MENU = fn.glyph(ns.texture("gui/generic_9x2_page_menu"), 148, 13)
    }
}