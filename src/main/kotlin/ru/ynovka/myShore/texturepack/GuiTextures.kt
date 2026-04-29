package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.asset.Font
import net.kyori.adventure.text.TextComponent
import ru.ynovka.myShore.texturepack.TexturePack.ns


object GuiTextures {
    lateinit var MENU_1x9_028: TextComponent
    lateinit var MENU_HUB_PLAY: TextComponent
    lateinit var MENU_6x9: TextComponent
    lateinit var MENU_WD_MAIN: TextComponent
    lateinit var MENU_WD_SELECT_CITY: TextComponent

    fun register() {
        val fn: Font = ns.font("gui", false)

        MENU_1x9_028 = fn.glyph(ns.texture("gui/menu_1x9_028"), 130, 13).toComponent()
        MENU_HUB_PLAY = fn.glyph(ns.texture("gui/menu_hub_play"), 148, 13).toComponent()
        MENU_6x9 = fn.glyph(ns.texture("gui/menu_6x9"), 220, 13).toComponent()
        MENU_WD_MAIN = fn.glyph(ns.texture("gui/menu_wd_main"), 185, 13).toComponent()
        MENU_WD_SELECT_CITY = fn.glyph(ns.texture("gui/menu_wd_select_city"), 185, 13).toComponent()
    }
}