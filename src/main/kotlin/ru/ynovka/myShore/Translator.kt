package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import ru.ynovka.myShore.MyShore.Companion.inst


object Translator {
    fun register() {
        ServerTranslator.loadResource(inst, "lang/en_us.properties")
        ServerTranslator.loadResource(inst, "lang/ru_ru.properties")
    }
}