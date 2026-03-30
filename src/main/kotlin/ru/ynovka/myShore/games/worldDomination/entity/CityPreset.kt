package ru.ynovka.myShore.games.worldDomination.entity

import net.kyori.adventure.text.TranslatableComponent

data class CityPreset(
    val name: TranslatableComponent,
    val capitalizationRange: IntRange = 250..275
)