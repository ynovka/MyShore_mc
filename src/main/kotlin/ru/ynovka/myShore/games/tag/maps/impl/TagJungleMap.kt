package ru.ynovka.myShore.games.tag.maps.impl

import com.github.darksoulq.abyssallib.world.item.component.builtin.CooldownUse
import io.papermc.paper.datacomponent.item.UseCooldown.useCooldown
import ru.ynovka.myShore.games.tag.TagGame.Companion.currentTagGame
import com.github.darksoulq.abyssallib.server.event.ActionResult
import ru.ynovka.myShore.games.tag.TagPlayerRoles
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.games.tag.maps.MapSpawn
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.tag.maps.TagMap
import ru.ynovka.myShore.games.tag.findPlayer
import java.util.concurrent.ThreadLocalRandom
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.utils.cancelItem
import net.kyori.adventure.text.Component
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionEffect
import org.bukkit.entity.ItemDisplay
import kotlin.random.asKotlinRandom
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.GameMode
import org.bukkit.Material
import org.joml.Matrix4f
import org.bukkit.Bukkit
import org.bukkit.Sound
import kotlin.math.abs


object TagJungleMap : TagMap {

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

    override fun onGameStart(game: TagGame) = spawnPoisonDarts(game)
    override fun onGameEnd(game: TagGame) = removeDarts(game)
    override fun onPlayerJoin(game: TagGame, player: Player) = showDartsFromPlayer(player)
    override fun onPlayerLeave(game: TagGame, player: Player) = hideDartsFromPlayer(player)

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
        val victimCount = game.gamePlayers.count { it.role == TagPlayerRoles.VICTIM }
        val count = victimCount * 2

        val world = Bukkit.getWorld(game.map.mapId) ?: return
        val dart = Items.poisonDart.getStack(null)

        val d = mutableSetOf<ItemDisplay>()
        poisonDartSpawns.shuffled().take(count)
            .forEach { spawn ->
                world.spawn(spawn.toLocation(), ItemDisplay::class.java) { display ->
                    display.isPersistent = false
                    display.setItemStack(dart.clone())
                    display.isVisibleByDefault = false
                    game.gamePlayers.forEach { it.player.showEntity(inst, display) }
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

    fun showDartsFromPlayer(player: Player) {
        darts.values.flatten().forEach { display ->
            if (display.isValid) player.showEntity(inst, display)
        }
    }

    override fun registerEvents() = Events.register()

    object Events {
        fun register() {
            val duration = 20
            val startTime = System.currentTimeMillis()

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
                Bukkit.getWorld(mapId)?.players?.forEach { player ->
                    if (player.gameMode != GameMode.ADVENTURE) return@forEach
                    val game = player.currentTagGame() ?: return@forEach
                    val tagPlayer = game.findPlayer(player) ?: return@forEach
                    if (tagPlayer.role != TagPlayerRoles.VICTIM) return@forEach
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

    override fun registerItems() = Items.register()

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
            player.addPotionEffect(this.random(random))
        }

        private fun usePotionDart(player: Player, itemSlot: EquipmentSlot) {
            val i = player.inventory.getItem(itemSlot)
            if (i.type == Material.AIR || player.hasCooldown(i)) return
            val game = player.currentTagGame() ?: return
            player.setCooldown(Key.key(inst, "tag_jungle_poison_dart"), 20 * 20)
            game.gamePlayers.forEach { it.player.playSound(player.location, Sound.BLOCK_BAMBOO_HIT, 1f, 2f) }
            player.inventory.clear(0)
            val target = player.getTargetEntity(25, false) as? Player ?: return
            poisonDartEffects.applyRandomTo(target)
        }
    }
}