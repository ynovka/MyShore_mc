package ru.ynovka.myShore.texturepack

import com.github.darksoulq.abyssallib.server.resource.asset.SoundEvent
import ru.ynovka.myShore.texturepack.TexturePack.sounds


object SoundsPack {
    lateinit var RIFT_SOUND: SoundEvent

    fun register() {
        RIFT_SOUND = sounds.event("rift")
        RIFT_SOUND.addVariant("rift")
    }
}