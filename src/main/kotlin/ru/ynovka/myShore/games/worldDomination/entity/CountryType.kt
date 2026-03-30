package ru.ynovka.myShore.games.worldDomination.entity

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.Location


enum class CountryType(
    val nameTranslatable: TranslatableComponent,
    val location: Location,
    val cityPresets: List<CityPreset>
) {
    RUSSIA(
        Component.translatable("name.myshore.wd.country.russia"),
        Location(null, 1000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.moscow"), 250..260),
            CityPreset(Component.translatable("name.myshore.wd.city.spb"), 248..258),
            CityPreset(Component.translatable("name.myshore.wd.city.novosibirsk"), 245..255),
            CityPreset(Component.translatable("name.myshore.wd.city.yekaterinburg"), 247..257)
        )
    ),
    USA(
        Component.translatable("name.myshore.wd.country.usa"),
        Location(null, 2000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.new_york"), 262..272),
            CityPreset(Component.translatable("name.myshore.wd.city.la"), 260..270),
            CityPreset(Component.translatable("name.myshore.wd.city.chicago"), 258..268),
            CityPreset(Component.translatable("name.myshore.wd.city.houston"), 257..267)
        )
    ),
    JAPAN(
        Component.translatable("name.myshore.wd.country.japan"),
        Location(null, 3000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.tokyo"), 260..270),
            CityPreset(Component.translatable("name.myshore.wd.city.osaka"), 258..268),
            CityPreset(Component.translatable("name.myshore.wd.city.yokohama"), 256..266),
            CityPreset(Component.translatable("name.myshore.wd.city.nagoya"), 255..265)
        )
    ),
    GERMANY(
        Component.translatable("name.myshore.wd.country.germany"),
        Location(null, 4000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.berlin"), 259..269),
            CityPreset(Component.translatable("name.myshore.wd.city.hamburg"), 257..267),
            CityPreset(Component.translatable("name.myshore.wd.city.munich"), 258..268),
            CityPreset(Component.translatable("name.myshore.wd.city.cologne"), 256..266)
        )
    ),
    NORTH_KOREA(
        Component.translatable("name.myshore.wd.country.north_korea"),
        Location(null, 5000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.pyongyang"), 244..254),
            CityPreset(Component.translatable("name.myshore.wd.city.hamhung"), 242..252),
            CityPreset(Component.translatable("name.myshore.wd.city.chongjin"), 241..251),
            CityPreset(Component.translatable("name.myshore.wd.city.nampo"), 240..250)
        )
    ),
    CUBA(
        Component.translatable("name.myshore.wd.country.cuba"),
        Location(null, 6000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.havana"), 248..258),
            CityPreset(Component.translatable("name.myshore.wd.city.santiago_de_cuba"), 246..256),
            CityPreset(Component.translatable("name.myshore.wd.city.camaguey"), 244..254),
            CityPreset(Component.translatable("name.myshore.wd.city.holguin"), 243..253)
        )
    ),
    KAZAKHSTAN(
        Component.translatable("name.myshore.wd.country.kazakhstan"),
        Location(null, 7000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.almaty"), 249..259),
            CityPreset(Component.translatable("name.myshore.wd.city.astana"), 248..258),
            CityPreset(Component.translatable("name.myshore.wd.city.shymkent"), 245..255),
            CityPreset(Component.translatable("name.myshore.wd.city.karaganda"), 244..254)
        )
    ),
    BAHAMAS(
        Component.translatable("name.myshore.wd.country.bahamas"),
        Location(null, 8000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.nassau"), 250..260),
            CityPreset(Component.translatable("name.myshore.wd.city.freeport"), 247..257),
            CityPreset(Component.translatable("name.myshore.wd.city.marsh_harbour"), 244..254),
            CityPreset(Component.translatable("name.myshore.wd.city.george_town"), 243..253)
        )
    ),
    ISRAEL(
        Component.translatable("name.myshore.wd.country.israel"),
        Location(null, 9000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.jerusalem"), 258..268),
            CityPreset(Component.translatable("name.myshore.wd.city.tel_aviv"), 260..270),
            CityPreset(Component.translatable("name.myshore.wd.city.haifa"), 255..265),
            CityPreset(Component.translatable("name.myshore.wd.city.beer_sheva"), 253..263)
        )
    ),
    AFGHANISTAN(
        Component.translatable("name.myshore.wd.country.afghanistan"),
        Location(null, 10000.0, 100.0, 0.0),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.kabul"), 242..252),
            CityPreset(Component.translatable("name.myshore.wd.city.kandahar"), 240..250),
            CityPreset(Component.translatable("name.myshore.wd.city.herat"), 241..251),
            CityPreset(Component.translatable("name.myshore.wd.city.mazar"), 240..250)
        )
    );
}