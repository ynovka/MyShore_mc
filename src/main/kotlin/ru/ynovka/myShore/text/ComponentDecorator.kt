package ru.ynovka.myShore.text

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import net.kyori.adventure.text.TranslatableComponent
import ru.ynovka.myShore.text.actionBar.CharWidth
import net.kyori.adventure.text.TextComponent
import ru.ynovka.myShore.texturepack.Glyphs
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.MyShore
import org.bukkit.entity.Player


object ComponentDecorator {

    // ── Публичное API ────────────────────────────────────────────────────────

    /** Добавляет фон к TranslatableComponent (переводится на русский перед обработкой). */
    fun addBackground(component: TranslatableComponent, player: Player): Component =
        addBackground(ServerTranslator.translate(component, player))

    /** Добавляет фон к произвольному Adventure-компоненту. */
    fun addBackground(component: Component): Component {
        return Component.text()
            .append(buildLeft())
            .append(decorate(component))
            .append(buildRight())
            .build()
    }
    // ── Внутренняя логика ────────────────────────────────────────────────────

    private fun decorate(component: Component): Component = when {
        component == Component.newline() -> Component.text()
            .append(buildRight())
            .append(Component.newline())
            .append(buildLeft())
            .build()

        component is TextComponent -> decorateText(component)
        else -> component.children(component.children().map { decorate(it) })
    }

    private fun decorateText(component: TextComponent): Component {
        val content = component.content()
        if (content.isEmpty()) {
            return component.children(component.children().map { decorate(it) })
        }

        val wrapper = Component.text().style(component.style()).content("")

        content.forEach { char ->
            if (char == '\n') {
                wrapper.append(buildRight())
                wrapper.append(Component.newline())
                wrapper.append(buildLeft())
            } else {
                val w = CharWidth.of(char)
                wrapper.append(buildBgEntry(w))
                wrapper.append(Component.text(char.toString()))
            }
        }

        component.children().forEach { wrapper.append(decorate(it)) }
        return wrapper.build()
    }

    private fun buildLeft(): Component =
        MyShore.Companion.mm.deserialize("<white><shadow:#00000000>${Glyphs.BACKGROUND_LEFT}</shadow></white>")

    private fun buildRight(): Component =
        MyShore.Companion.mm.deserialize("<white><shadow:#00000000>${Glyphs.BACKGROUND_RIGHT}</shadow></white>")

    /**
     * Возвращает: BACKGROUND_CENTER[w] + returnShift, цвет принудительно белый.
     * Белый нужен, чтобы глиф не тонировался цветом родителя.
     */
    private fun buildBgEntry(w: Int): Component {
        val bgStr = Glyphs.BACKGROUND_CENTER[w] ?: Glyphs.BACKGROUND_CENTER[6]!!
        val shift = TextOffset.getOffsetMinimessage(-(w - 1))
        return MyShore.Companion.mm.deserialize("<white><shadow:#00000000>$bgStr$shift</shadow></white>")
    }
}