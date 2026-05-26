package ru.ynovka.myShore.game.gameUtils

import org.bukkit.event.entity.EntityDamageByEntityEvent
import ru.ynovka.myShore.MyShore.Companion.scheduler
import org.bukkit.event.EventHandler
import org.bukkit.entity.Firework
import ru.ynovka.myShore.MyShore
import org.bukkit.FireworkEffect
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI


fun spawnFireworksAround(player: Player) {
    scheduler.schedule {
        if (!player.isOnline || !player.isValid) {
            return@schedule
        }

        val center = player.location.clone().add(0.0, 0.4, 0.0)

        val count = 14
        val radius = 3.0

        repeat(count) { index ->
            val angle = 2.0 * PI * index / count

            val x = cos(angle) * radius
            val z = sin(angle) * radius

            val spawnLocation = center.clone().add(x, 0.0, z)

            scheduler.schedule {
                spawnPrettyFirework(spawnLocation)
            }.region(spawnLocation).once()
        }
    }.entity(player).once()
}

private fun spawnPrettyFirework(location: Location) {
    val world = location.world ?: return

    val firework = world.spawn(location, Firework::class.java)

    firework.addScoreboardTag("cosmetic_firework")

    val meta = firework.fireworkMeta

    meta.power = 1
    meta.addEffect(
        FireworkEffect.builder()
            .with(randomFireworkType())
            .withColor(randomColor(), randomColor())
            .withFade(randomColor())
            .trail(true)
            .flicker(true)
            .build()
    )

    firework.fireworkMeta = meta

    firework.isShotAtAngle = false

    firework.velocity = Vector(
        Random.nextDouble(-0.08, 0.08),
        Random.nextDouble(0.75, 1.05),
        Random.nextDouble(-0.08, 0.08)
    )

    firework.ticksToDetonate = Random.nextInt(18, 29)
}

private fun randomFireworkType(): FireworkEffect.Type {
    return listOf(
        FireworkEffect.Type.BALL,
        FireworkEffect.Type.BALL_LARGE,
        FireworkEffect.Type.BURST,
        FireworkEffect.Type.STAR
    ).random()
}

private fun randomColor(): Color {
    return listOf(
        Color.AQUA,
        Color.BLUE,
        Color.FUCHSIA,
        Color.LIME,
        Color.ORANGE,
        Color.PURPLE,
        Color.RED,
        Color.WHITE,
        Color.YELLOW
    ).random()
}

object CosmeticFireworkListener : Listener {

    fun register() {
        MyShore.inst.server.pluginManager.registerEvents(this, MyShore.Companion.inst)
    }

    @EventHandler(ignoreCancelled = true)
    fun onDamage(event: EntityDamageByEntityEvent) {
        val firework = event.damager as? Firework ?: return

        if (firework.scoreboardTags.contains("cosmetic_firework")) {
            event.isCancelled = true
        }
    }
}
