package ru.ynovka.myShore.text


object CharWidth {

    private val table: Map<Char, Int> = buildMap {

        "!',.;:i\u2019".forEach { put(it, 2) }

        "`▪l".forEach { put(it, 3) }

        " ()*[]It{}◘".forEach { put(it, 4) }

        "fk".forEach { put(it, 5) }
        "гк".forEach { put(it, 5) }

        "+-±#%/?■0123456789\$=^_∈≥≤÷ψ".forEach { put(it, 6) }

        "abcdeghj mnopqrsuvwxyz".replace(" ", "").forEach { put(it, 6) }

        "ABCDEFGHJKLMNOPQRSTUVWXYZmM".forEach { put(it, 6) }

        "абвеёжзийлмнопрстуфхцчшьэя".forEach { put(it, 6) }

        "АБВГЕЗИЙКЛНОПРСТУХЧЬЭЯМ".forEach { put(it, 6) }

        "~√ъ@«»≡≈Ω✔ДЦЪдыщ".forEach { put(it, 7) }
        put('\uD83D', 7)
        put('\uDD25', 7)

        "❤⌀★☠⏻⌚ЖФШюЫЮ".forEach { put(it, 8) }

        put('Щ', 9)

        "⚠☯☒☐☑№".forEach { put(it, 10) }
    }

    fun of(char: Char): Int = table[char] ?: 6
}