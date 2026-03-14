package ru.ynovka.myShore.games.tag.maps.impl

import com.github.darksoulq.abyssallib.world.item.component.builtin.CooldownUse
import com.github.darksoulq.abyssallib.world.item.component.builtin.Consume
import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.world.item.item
import io.papermc.paper.datacomponent.item.Consumable.consumable
import io.papermc.paper.datacomponent.item.UseCooldown.useCooldown
import ru.ynovka.myShore.games.tag.maps.TagGameMap
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.MyShore.Companion.inst
import java.util.concurrent.ThreadLocalRandom
import org.bukkit.potion.PotionEffectType
import ru.ynovka.myShore.utils.cancelItem
import org.bukkit.inventory.EquipmentSlot
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.MapSpawn
import org.bukkit.potion.PotionEffect
import kotlin.random.asKotlinRandom
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import kotlin.collections.listOf
import org.bukkit.entity.Player
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.lobby.getLobby


object JungleMap : TagGameMap {

    override val mapId = "tag_jungle"
    override val mapName = Component.translatable("name.myshore.tag.map.jungle")

    override val authors = listOf(
        "Ynovka",
        "_JuliA_"
    )

    override val hunterSpawn = MapSpawn(
        "tag_jungle",
        -2.5, 106.0, -42.5,
        0f, 0f
    )

    override val victimSpawns = listOf(
        MapSpawn("tag_jungle", 3.5, 102.0, 0.5, 180f, 0f),
        MapSpawn("tag_jungle", 0.5, 101.0, 0.5, 180f, 0f),
        MapSpawn("tag_jungle", -2.5, 101.0, 0.5, 180f, 0f),
        MapSpawn("tag_jungle", -4.5, 101.0, -1.5, 180f, 0f)
    )

    val poisonDartSpawns = listOf(
        MapSpawn("tag_jungle", -12.5, 106.5, -10.5),
        MapSpawn("tag_jungle", -22.5, 107.5, -6.5),
        MapSpawn("tag_jungle", -16.5, 106.5, -1.5),
        MapSpawn("tag_jungle", -16.5, 109.5, 11.5),
        MapSpawn("tag_jungle", -2.5, 112.5, 9.5),
        MapSpawn("tag_jungle", 12.5, 109.5, 12.5),
        MapSpawn("tag_jungle", 10.5, 110.5, -8.5),
        MapSpawn("tag_jungle", -2.5, 105.5, 6.5),
        MapSpawn("tag_jungle", -2.5, 109.5, -20.5),
        MapSpawn("tag_jungle", 11.5, 109.5, -24.5),
        MapSpawn("tag_jungle", 5.5, 118.5, -32.5),
        MapSpawn("tag_jungle", -7.5, 119.5, -28.5),
        MapSpawn("tag_jungle", -19.5, 122.5, -22.5),
        MapSpawn("tag_jungle", -18.5, 124.0, -42.5),
        MapSpawn("tag_jungle", -8.5, 110.5, -42.5),
        MapSpawn("tag_jungle", 0.5, 108.5, -39.5),
        MapSpawn("tag_jungle", -7.5, 107.5, -28.5),
        MapSpawn("tag_jungle", -18.5, 106.5, -22.5),
        MapSpawn("tag_jungle", -9.5, 109.5, -38.5)
    )

    fun spawnPoisonDarts(game: TagGame) {
        val victims = game.players.filter { it.value == TagPlayerRoles.VICTIM }.size
        val count = victims * 2

        val world = Bukkit.getWorld(game.map.mapId) ?: return
        val dart = Items.poisonDart.getStack(null)

        poisonDartSpawns.shuffled().take(count).map { it.toLocation() }
            .forEach { location ->
                world.dropItemNaturally(location, dart.clone())
            }
    }

    object Items {
        fun register() {
            TexturePack.createItemTexture(poisonDart)
            ITEMS.register("tag_jungle_poison_dart") { poisonDart }
        }

        val poisonDart = cancelItem(Key.key(inst, "tag_jungle_poison_dart")) {
            component(CooldownUse(useCooldown(20f).cooldownGroup(Key.key(inst, "tag_jungle_poison_dart")).build()))
            tooltip { player ->
                line(Component.translatable("desc.myshore.tag_jungle_poison_dart.1"))
                line(Component.translatable("desc.myshore.tag_jungle_poison_dart.2"))
                line(Component.translatable("desc.myshore.tag_jungle_poison_dart.3"))
            }
            onUse { source, hand, _ ->
                usePotionDart((source as Player), hand)
                ActionResult.CANCEL
            }
            onPickup { player ->
                val game = player.getLobby()?.game as? TagGame ?: return@onPickup ActionResult.CANCEL
                if (game.players[player.uniqueId] != TagPlayerRoles.VICTIM) return@onPickup ActionResult.CANCEL
                if (player.inventory.getItem(0) != null) return@onPickup ActionResult.CANCEL
                ActionResult.PASS
            }
        }

        private val poisonDartEffects = listOf(
            PotionEffect(PotionEffectType.SPEED, 40, 0, false, false, false),
            PotionEffect(PotionEffectType.SLOWNESS, 30, 0, false, false, false),
            PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0, false, false, false),
            PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false, false),
            PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0, false, false, false),
            PotionEffect(PotionEffectType.JUMP_BOOST, 40, 0, false, false, false),
        )

        private fun List<PotionEffect>.applyRandomTo(player: Player) {
            val random = ThreadLocalRandom.current().asKotlinRandom()
            val effect = this.random(random)

            player.addPotionEffect(effect)
        }

        private fun usePotionDart(player: Player, itemSlot: EquipmentSlot) {
            val i = player.inventory.getItem(itemSlot)
            if (i.type == Material.AIR || player.hasCooldown(i)) return
            player.setCooldown(Key.key(inst, "tag_jungle_poison_dart"), 20*20)
            val target = player.getTargetEntity(25, false) as? Player ?: return
            poisonDartEffects.applyRandomTo(target)
            // playSound "Фью!"
        }
    }
}
