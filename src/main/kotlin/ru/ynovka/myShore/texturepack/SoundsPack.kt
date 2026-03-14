package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.asset.Sounds
import ru.ynovka.myShore.texturepack.TexturePack.sounds


object SoundsPack {
    lateinit var RIFT_SOUND: Sounds.Sound

    fun register() {
        RIFT_SOUND = sounds.sound("rift")
    }

}