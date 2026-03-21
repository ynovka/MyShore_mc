package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.server.resource.asset.Font
import ru.ynovka.myShore.texturepack.TexturePack.fontGlyphs
import ru.ynovka.myShore.texturepack.TexturePack.ns


object Glyphs {
    lateinit var BACKGROUND_LEFT:  String
        private set
    val BACKGROUND_CENTER: MutableMap<Int, String> = mutableMapOf()
    lateinit var BACKGROUND_RIGHT:  String
        private set

    fun register() {
        BACKGROUND_LEFT = newGlyph("background_left", 14, 10)
            .toMiniMessageString() + TextOffset.getOffsetMinimessage(-1)
        for (i in 2..9) {
            BACKGROUND_CENTER[i] = newGlyph("background_center_$i", 14, 10)
                .toMiniMessageString() + TextOffset.getOffsetMinimessage(-2)
        }
        BACKGROUND_RIGHT = newGlyph("background_right", 14, 10)
            .toMiniMessageString() + TextOffset.getOffsetMinimessage(-1)
    }

    fun newGlyph(name: String, height: Int = 8, ascent: Int = 8): Font.TextureGlyph {
        return fontGlyphs.glyph(ns.texture("glyphs/$name"), height, ascent)
    }
}
