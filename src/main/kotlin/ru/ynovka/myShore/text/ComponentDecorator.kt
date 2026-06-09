package ru.ynovka.myShore.text

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.extension.plain
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import ru.ynovka.myShore.texturepack.Glyphs
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player


val BACKGROUND_WIDTHS = intArrayOf(128, 64, 32, 16, 8, 4, 2, 1)

fun Component.withBackground(): Component {
    val length = CharWidth.width(this.plain)
    val background = backgroundComponent(length)
    return Component.empty()
        .append(Glyphs.BACKGROUND_LEFT)
        .append(background)
        .append(Glyphs.BACKGROUND_RIGHT)
        .color(NamedTextColor.WHITE)
        .shadowColor(ShadowColor.fromHexString("#00000000"))
        .append(TextOffset.getOffset(-1 * length - 6))
        .append(this)
}

fun TranslatableComponent.withBackground(player: Player): Component =
    ServerTranslator.translate(this, player.locale()).withBackground()

fun Component.translate(player: Player): Component =
    ServerTranslator.translate(this, player.locale())

fun backgroundComponent(length: Int): Component =
    CharWidth.splitWidth(length)
        .fold(Component.empty()) { component, width ->
            component.append(Glyphs.BACKGROUND_CENTER.getValue(width))
        }
