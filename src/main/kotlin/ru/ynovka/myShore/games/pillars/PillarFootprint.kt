package ru.ynovka.myShore.games.pillars

typealias Footprint = Set<Pair<Int, Int>>

object Footprints {
    val single: Footprint = setOf(0 to 0)

    /** Правильный шестиугольник, radius — кол-во колец от центра */
    fun hexagon(radius: Int): Footprint = buildSet {
        for (q in -radius..radius) {
            val rMin = maxOf(-radius, -q - radius)
            val rMax = minOf(radius, -q + radius)
            for (r in rMin..rMax) {
                val x = q + (r - (r and 1)) / 2
                add(x to r)
            }
        }
    }

    /** Квадрат halfSize×halfSize вокруг центра */
    fun square(halfSize: Int): Footprint = buildSet {
        for (dx in -halfSize..halfSize)
            for (dz in -halfSize..halfSize)
                add(dx to dz)
    }
}