package ru.ynovka.myShore.text

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import ru.ynovka.myShore.texturepack.Glyphs
import ru.ynovka.myShore.texturepack.Glyphs.BACKGROUND_LEFT
import ru.ynovka.myShore.texturepack.Glyphs.BACKGROUND_RIGHT

/**
 * Оборачивает Adventure-компонент фоновыми глифами переменной ширины.
 *
 * ── Почему НЕ через сериализацию в строку ──────────────────────────────────
 * MiniMessage.serialize() → deserialize() разрушает ClickEvent / HoverEvent,
 * потому что эти события не имеют MiniMessage-представления без потерь.
 * Здесь мы рекурсивно обходим дерево компонентов и МОДИФИЦИРУЕМ его напрямую.
 *
 * ── Математика сдвигов ─────────────────────────────────────────────────────
 * BACKGROUND_CENTER[W] = <glyph background_center_W> + TextOffset(-2)
 *   • Продвижение курсора глифом  = W + 1  (ширина изображения W + 1px межбуквенный)
 *   • Встроенный сдвиг в BACKGROUND_CENTER = -2
 *   • Итого после BACKGROUND_CENTER[W]: курсор сдвинулся на (W + 1 - 2) = W - 1
 * Нужно вернуть курсор в исходную позицию: returnShift = -(W - 1)
 * После этого символ рисуется и продвигает курсор на W px — всё верно.
 *
 * Если рендер "плывёт" — измени формулу returnShift в [buildBgEntry].
 */
object ComponentDecorator {

    private val mm = MiniMessage.miniMessage()

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
        mm.deserialize("<white><shadow:#00000000>$BACKGROUND_LEFT</shadow></white>")

    private fun buildRight(): Component =
        mm.deserialize("<white><shadow:#00000000>$BACKGROUND_RIGHT</shadow></white>")

    /**
     * Возвращает: BACKGROUND_CENTER[w] + returnShift, цвет принудительно белый.
     * Белый нужен, чтобы глиф не тонировался цветом родителя.
     */
    private fun buildBgEntry(w: Int): Component {
        val bgStr = Glyphs.BACKGROUND_CENTER[w] ?: Glyphs.BACKGROUND_CENTER[6]!!
        val shift = TextOffset.getOffsetMinimessage(-(w - 1))
        return mm.deserialize("<white><shadow:#00000000>$bgStr$shift</shadow></white>")
    }
}