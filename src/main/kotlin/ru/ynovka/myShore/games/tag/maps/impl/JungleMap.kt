package ru.ynovka.myShore.games.tag.maps.impl

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.world.item.component.builtin.CooldownUse
import io.papermc.paper.datacomponent.item.UseCooldown.useCooldown
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.joml.Matrix4f
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.games.tag.maps.TagGameMap
import ru.ynovka.myShore.lobby.getLobby
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.utils.MapSpawn
import ru.ynovka.myShore.utils.Utils.asPlayer
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.utils.cancelItem
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.random.asKotlinRandom


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

    val darts: MutableMap<TagGame, MutableSet<ItemDisplay>> = mutableMapOf()

    fun spawnPoisonDarts(game: TagGame) {
        val victims = game.players.filter { it.value == TagPlayerRoles.VICTIM }.keys
        val count = victims.size * 2

        val world = Bukkit.getWorld(game.map.mapId) ?: return
        val dart = Items.poisonDart.getStack(null)

        val d = mutableSetOf<ItemDisplay>()
        poisonDartSpawns.shuffled().take(count)
            .forEach { spawn ->
                world.spawn(spawn.toLocation(), ItemDisplay::class.java) { display ->
                    display.isPersistent = false
                    display.setItemStack(dart.clone())
                    display.isVisibleByDefault = false
                    victims.forEach { it.asPlayer()?.showEntity(inst, display) }
                    d.add(display)
                }
            }

        darts[game] = d
    }

    fun removeDarts(game: TagGame) {
        darts[game]?.forEach { display ->
            if (display.isValid) display.remove()
        }
        darts.remove(game)
    }

    fun hideDartsFromPlayer(player: Player) {
        darts.values.flatten().forEach { display ->
            if (display.isValid) player.hideEntity(inst, display)
        }
    }

    object Events {
        fun register() {
            val duration = 20
            val startTime = System.currentTimeMillis()
            val world = Bukkit.getServer().getWorld(mapId)!!

            Bukkit.getScheduler().runTaskTimer(inst, Runnable {
                val angle = ((System.currentTimeMillis() - startTime) / 5L % 360).toFloat() * (Math.PI.toFloat() / 180f)
                val mat = Matrix4f().scale(0.5f).rotateY(angle)
                val gameIterator = darts.iterator()
                while (gameIterator.hasNext()) {
                    val (_, set) = gameIterator.next()
                    val setIterator = set.iterator()
                    while (setIterator.hasNext()) {
                        val display = setIterator.next()
                        if (!display.isValid) {
                            setIterator.remove()
                            continue
                        }
                        display.interpolationDelay = 0
                        display.interpolationDuration = duration
                        display.setTransformationMatrix(mat)
                    }
                    if (set.isEmpty()) gameIterator.remove()
                }
            }, 1L, duration.toLong())

            Bukkit.getScheduler().runTaskTimer(inst, Runnable {
                world.players.forEach { player ->
                    if (player.gameMode != GameMode.ADVENTURE) return@forEach
                    val game = player.getLobby()?.game as? TagGame ?: return@forEach
                    if (game.players[player.uniqueId] != TagPlayerRoles.VICTIM) return@forEach
                    if (player.inventory.getItem(0) != null) return@forEach
                    val d = darts[game] ?: return@forEach
                    val dart = d.firstOrNull { display ->
                        val loc = display.location
                        val p = player.location
                        abs(loc.x - p.x) < 0.5 && abs(loc.y - p.y) < 1.5 && abs(loc.z - p.z) < 0.5
                    } ?: return@forEach
                    dart.remove()
                    d.remove(dart)
                    player.inventory.setItem(0, Items.poisonDart.getStack(null))
                    player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
                }
            }, 1L, 2L)
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
        }

        private val poisonDartEffects = listOf(
            PotionEffect(PotionEffectType.SPEED, 100, 1, false, false, false),
            PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false, false, false),
            PotionEffect(PotionEffectType.SLOW_FALLING, 100, 1, false, false, false),
            PotionEffect(PotionEffectType.DARKNESS, 100, 1, false, false, false),
            PotionEffect(PotionEffectType.NIGHT_VISION, 100, 1, false, false, false),
            PotionEffect(PotionEffectType.JUMP_BOOST, 100, 1, false, false, false)
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
            player.inventory.clear(0)
            poisonDartEffects.applyRandomTo(target)
            // playSound "Фью!"
            val game = player.getLobby()?.game as? TagGame ?: return
            game.players.keys.asPlayers().forEach { p ->
                p.playSound(player.location, Sound.BLOCK_BAMBOO_HIT, 1f, 2f)
            }
        }
    }
}
