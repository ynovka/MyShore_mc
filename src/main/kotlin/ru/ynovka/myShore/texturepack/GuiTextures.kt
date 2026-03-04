package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.Namespace
import com.github.darksoulq.abyssallib.server.resource.asset.Font

object GuiTextures {
    var PLAY_MENU: Font.TextureGlyph? = null
    var TAG_CHOOSE_ROLE_MENU: Font.TextureGlyph? = null

    fun register(ns: Namespace) {
        val fn: Font = ns.font("gui", false)

        PLAY_MENU = fn.glyph(ns.texture("gui/play"), 148, 13)
        TAG_CHOOSE_ROLE_MENU = fn.glyph(ns.texture("gui/tag_choose_role"), 130, 13)
    }
}