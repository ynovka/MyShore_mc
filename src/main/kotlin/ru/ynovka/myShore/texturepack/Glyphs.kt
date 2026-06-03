package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.server.resource.asset.Font
import ru.ynovka.myShore.texturepack.TexturePack.fontGlyphs
import ru.ynovka.myShore.texturepack.TexturePack.ns
import ru.ynovka.myShore.text.BACKGROUND_WIDTHS
import net.kyori.adventure.text.TextComponent


object Glyphs {
    lateinit var BACKGROUND_LEFT: TextComponent
        private set
    val BACKGROUND_CENTER: MutableMap<Int, TextComponent> = mutableMapOf()
    lateinit var BACKGROUND_RIGHT: TextComponent
        private set

    fun register() {
        BACKGROUND_LEFT = newGlyph("background_left", 14, 10)
            .toComponent().append(TextOffset.getOffset(-1))

        for (i in BACKGROUND_WIDTHS) {
            BACKGROUND_CENTER[i] = newGlyph("background_center_$i", 14, 10)
                .toComponent().append(TextOffset.getOffset(-1))
        }

        BACKGROUND_RIGHT = newGlyph("background_right", 14, 10)
            .toComponent().append(TextOffset.getOffset(-1))
    }

    fun newGlyph(name: String, height: Int = 8, ascent: Int = 8): Font.TextureGlyph {
        return fontGlyphs.glyph(ns.texture("glyphs/$name"), height, ascent)
    }
}
