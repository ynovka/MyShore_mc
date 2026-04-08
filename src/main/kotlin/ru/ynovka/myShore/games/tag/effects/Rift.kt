package ru.ynovka.myShore.games.tag.effects

import com.github.darksoulq.abyssallib.world.particle.ParticleRenderer
import com.github.darksoulq.abyssallib.world.particle.impl.Renderers
import com.github.darksoulq.abyssallib.world.particle.Generator
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Color
import particles
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GamePlayer.Companion.asPlayers


object RiftEffect {
    private val RIFT_COLOR   = Color.fromRGB(0x8A, 0xEB, 0xFF)
    private val FLASH_COLOR  = Color.fromRGB(138, 234, 255)

    fun play(game: Game<*>, player: Player) {
        val loc: Location = player.location.clone().add(0.0, 1.0, 0.0)
        particles {
            origin(loc)
            shape(scatterCloud(65, 0.7, 1.0, 0.7))
            render(dustTransitionRenderer(1.2f, RIFT_COLOR, RIFT_COLOR))
            duration(2L)
            interval(1L)
            viewers(game.gamePlayers.asPlayers())
        }.start()

        particles {
            origin(loc)
            shape(scatterCloud(20, 0.5, 0.7, 0.5))
            render(Renderers.Standard(Particle.GLOW, 1, 0.0, null))
            viewers(game.gamePlayers.asPlayers())
            duration(2L)
            interval(1L)
        }.start()

        particles {
            origin(loc)
            shape { _ -> listOf(Vector(0.0, 0.0, 0.0)) }
            render(
                Renderers.Standard(
                    Particle.FLASH,
                    1, 0.0,
                    FLASH_COLOR
                )
            )
            viewers(game.gamePlayers.asPlayers())
            duration(1L)
            interval(1L)
        }.start()
    }
}

private fun scatterCloud(count: Int, ox: Double, oy: Double, oz: Double): Generator =
    Generator { _ ->
        List(count) {
            Vector(
                (Random.nextDouble() * 2.0 - 1.0) * ox,
                (Random.nextDouble() * 2.0 - 1.0) * oy,
                (Random.nextDouble() * 2.0 - 1.0) * oz
            )
        }
    }

private fun dustTransitionRenderer(
    size: Float,
    from: Color,
    to: Color
): ParticleRenderer = ParticleRenderer { center, points, viewers ->
    for (v in points) {
        val loc = center.clone().add(v)
        for (viewer in viewers) {
            viewer.spawnParticle(
                Particle.DUST_COLOR_TRANSITION,
                loc,
                1,
                0.0, 0.0, 0.0,
                0.0,
                Particle.DustTransition(from, to, size)
            )
        }
    }
}