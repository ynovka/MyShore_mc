package ru.ynovka.myShore.game.worldDomination.entity

import com.github.darksoulq.abyssallib.world.advancement.AdvancementFrame
import com.github.darksoulq.abyssallib.world.advancement.Toast
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import ru.ynovka.myShore.utils.Utils.intValue


class City(
    /** Название города - ключ перевода */
    val name: TranslatableComponent,
    /** Ссылка на страну-родитель */
    val country: Country,
    private val startCapitalization: Int
) {
    /** Уровень города */
    var lvl = 0
        private set

    /** Жив ли город? */
    var isAlive = true
        private set

    var hasShield = false
        private set

    val capitalization
        get() = (startCapitalization + lvl * 100) * isAlive.intValue

    /** Бомбить город */
    fun bombardCity() {
        if (hasShield) {
            hasShield = false
            return
        }
        isAlive = false
        country.game.ecology -= ECOLOGY_DESTROY_PENALTY
    }

    /** @return true если щит успешно установлен */
    fun buyShield(): Toast {
        if (hasShield) return shieldAlredyExist
        if (country.balance < SHIELD_COST) return notEnoughMoneyToast
        country.balance -= SHIELD_COST
        hasShield = true
        country.game.history.record(WDHistoryEntry(WDAction.SHIELD_BUILT, country.game.round, actor = country, targetCity = this))
        return shieldBought
    }

    /** @return true если город успешно улучшен */
    fun buyUpgrade(): Toast {
        if (country.balance < UPGRADE_COST) return notEnoughMoneyToast
        country.balance -= UPGRADE_COST
        lvl += 1
        country.game.history.record(WDHistoryEntry(WDAction.CITY_UPGRADED, country.game.round, actor = country, targetCity = this))
        return upgradeBought
    }

    companion object {
        const val UPGRADE_COST: Int = 150
        const val SHIELD_COST: Int = 300
        const val ECOLOGY_DESTROY_PENALTY: Double = 0.05

        val notEnoughMoneyToast: Toast = Toast.builder()
            .titlle(Component.text("Недостаточно", NamedTextColor.WHITE))
            .subtitle(Component.text("средств!", NamedTextColor.WHITE))
            .icon(ItemStack.of(Material.RED_STAINED_GLASS_PANE))
            .frame(AdvancementFrame.GOAL)
            .build()

        val shieldAlredyExist: Toast = Toast.builder()
            .titlle(Component.text("На этом городе уже", NamedTextColor.WHITE))
            .subtitle(Component.text("есть щит!", NamedTextColor.WHITE))
            .icon(ItemStack.of(Material.RED_STAINED_GLASS_PANE))
            .frame(AdvancementFrame.GOAL)
            .build()

        val shieldBought: Toast = Toast.builder()
            .titlle(Component.text("Щит установлен", NamedTextColor.WHITE))
            .subtitle(Component.text("на город!", NamedTextColor.WHITE))
            .icon(ItemStack.of(Material.LIME_STAINED_GLASS_PANE))
            .frame(AdvancementFrame.CHALLENGE)
            .build()

        val upgradeBought: Toast = Toast.builder()
            .titlle(Component.text("Улучшение города", NamedTextColor.WHITE))
            .subtitle(Component.text("успешно куплено!", NamedTextColor.WHITE))
            .icon(ItemStack.of(Material.LIME_STAINED_GLASS_PANE))
            .frame(AdvancementFrame.CHALLENGE)
            .build()
    }
}