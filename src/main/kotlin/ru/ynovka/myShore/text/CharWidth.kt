package ru.ynovka.myShore.text


object CharWidth {
    private val table: Map<Int, Int> = buildMap {
        putChars("!',.;:i|", 2)

        putChars("`l‘’‚•′‵", 3)

        putChars(" \"()*[]It{}◘‹›", 4)

        putChars("<>fkгк", 5)

        putChars(
            "#\$%&+-/0123456789=?\\^_±■∈≥≤÷ψΩ" +
                    "abcdeghjmnopqrsuvwxyz" +
                    "ABCDEFGHJKLMNOPQRSTUVWXYZ" +
                    "абвеёжзийлмнопрстуфхцчшьэя" +
                    "АБВГЕЁЗИЙКЛМНОПРСТУХЧЬЭЯ",
            6
        )

        putChars("@~√«»≡≈✔–ДЦЪдыщъ", 7)

        putChars("❤⌀★☠⏻⌚…‰ЖФШЫЮю", 8)

        putChars("—Щ", 9)

        putChars("⚠☯☒☐☑№", 10)
    }

    private fun MutableMap<Int, Int>.putChars(chars: String, width: Int) {
        var i = 0
        while (i < chars.length) {
            val codePoint = chars.codePointAt(i)
            put(codePoint, width)
            i += Character.charCount(codePoint)
        }
    }

    fun of(codePoint: Int): Int = table[codePoint] ?: 6

    fun of(char: Char): Int = table[char.code] ?: 6

    fun width(text: String): Int {
        var result = 0
        var i = 0

        while (i < text.length) {
            val codePoint = text.codePointAt(i)
            result += of(codePoint)
            i += Character.charCount(codePoint)
        }

        return result
    }

    fun splitWidth(length: Int): List<Int> {
        require(length >= 0)

        var remaining = length
        val result = mutableListOf<Int>()

        for (width in BACKGROUND_WIDTHS) {
            while (remaining >= width) {
                result += width
                remaining -= width
            }
        }

        return result
    }
}