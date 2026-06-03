package ru.ynovka.myShore.game.gameUtils

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import org.bukkit.event.entity.EntityDamageByEntityEvent
import ru.ynovka.myShore.MyShore.Companion.scheduler
import org.bukkit.persistence.PersistentDataType
import org.bukkit.event.EventHandler
import org.bukkit.entity.Firework
import ru.ynovka.myShore.MyShore
import org.bukkit.FireworkEffect
import org.bukkit.event.Listener
import org.bukkit.NamespacedKey
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

            val launchDelayTicks = Random.nextLong(0L, 12L)

            scheduler.schedule {
                spawnPrettyFirework(spawnLocation)
            }
                .region(spawnLocation)
                .after(launchDelayTicks, Clock.TICKS)
                .once()
        }
    }.entity(player).once()
}

private val COSMETIC_FIREWORK_KEY = NamespacedKey(MyShore.inst, "cosmetic_firework")

private fun spawnPrettyFirework(location: Location) {
    val world = location.world ?: return

    val firework = world.spawn(location, Firework::class.java)

    firework.persistentDataContainer.set(
        COSMETIC_FIREWORK_KEY,
        PersistentDataType.BYTE,
        1
    )

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

    firework.ticksToDetonate = 20
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
        MyShore.inst.server.pluginManager.registerEvents(this, MyShore.inst)
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val firework = event.damager as? Firework ?: return

        val isCosmetic = firework.persistentDataContainer.has(
            COSMETIC_FIREWORK_KEY,
            PersistentDataType.BYTE
        )

        if (isCosmetic) event.isCancelled = true
    }
}