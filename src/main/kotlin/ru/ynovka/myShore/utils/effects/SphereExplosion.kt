package ru.ynovka.myShore.utils.effects

import com.github.darksoulq.abyssallib.world.particle.impl.Renderers
import com.github.darksoulq.abyssallib.world.particle.Particles
import com.github.darksoulq.abyssallib.world.particle.Generator
import ru.ynovka.myShore.lobby.Lobby
import org.bukkit.util.Vector
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Particle
import ru.ynovka.myShore.utils.Utils.asPlayers
import kotlin.math.*


object SphereExplosion {

    /**
     * @param count Общее количество частиц (N)
     * @param durationTicks Длительность анимации в тиках
     */
    fun spawn(lobby: Lobby, location: Location, count: Int, durationTicks: Long) {

        // 1. Предварительно рассчитываем целевые радиусы для каждой частицы.
        val targetRadii = DoubleArray(count) { i ->
            if (i < count / 3) {
                2.0
            } else {
                Random.nextDouble(2.0, 4.0)
            }
        }

        // 2. Создаём Generator (новый API ожидает List<Vector>)
        val explosionGenerator = Generator { tick ->
            val progress = min(1.0, tick.toDouble() / durationTicks)
            val phi = Math.PI * (3.0 - sqrt(5.0))

            val pts = ArrayList<Vector>(count)
            for (i in 0 until count) {
                val y = 1.0 - (i / (count - 1.0)) * 2.0
                val r = sqrt(max(0.0, 1.0 - y * y))
                val theta = phi * i

                val xDir = cos(theta) * r
                val zDir = sin(theta) * r

                val maxRadius = targetRadii[i]
                val currentDistance = maxRadius * progress

                pts.add(Vector(xDir * currentDistance, y * currentDistance, zDir * currentDistance))
            }
            pts
        }

        // 3. Рендер: используем стандартный рендерер, который вызывает World.spawnParticle для каждой точки.
        // Передаём count=1 чтобы каждая точка рендерилась одним particle (итого count точек -> count частиц).
        val renderer = Renderers.Standard(Particle.END_ROD, 1, 0.0, null)

        // 4. Строим Particles и запускаем
        Particles.builder()
            .origin(location)
            .shape(explosionGenerator)
            .render(renderer)
            .interval(1)
            .duration(durationTicks)
            .smooth(false)
            .viewers(lobby.members.asPlayers())
            .build()
            .start()
    }
}