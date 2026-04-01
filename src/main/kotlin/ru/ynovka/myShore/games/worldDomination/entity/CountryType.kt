package ru.ynovka.myShore.games.worldDomination.entity

import com.github.darksoulq.abyssallib.extension.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import ru.ynovka.myShore.texturepack.Glyphs.COUNTRY_FLAG


enum class CountryType(
    val flag: TextComponent,
    val nameTranslatable: TranslatableComponent,
    val cityPresets: List<CityPreset>
) {
    RUSSIA(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.russia"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.moscow"), 250..260),
            CityPreset(Component.translatable("name.myshore.wd.city.spb"), 248..258),
            CityPreset(Component.translatable("name.myshore.wd.city.novosibirsk"), 245..255),
            CityPreset(Component.translatable("name.myshore.wd.city.yekaterinburg"), 247..257)
        )
    ),
    USA(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.usa"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.new_york"), 262..272),
            CityPreset(Component.translatable("name.myshore.wd.city.la"), 260..270),
            CityPreset(Component.translatable("name.myshore.wd.city.chicago"), 258..268),
            CityPreset(Component.translatable("name.myshore.wd.city.houston"), 257..267)
        )
    ),
    JAPAN(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.japan"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.tokyo"), 260..270),
            CityPreset(Component.translatable("name.myshore.wd.city.osaka"), 258..268),
            CityPreset(Component.translatable("name.myshore.wd.city.yokohama"), 256..266),
            CityPreset(Component.translatable("name.myshore.wd.city.nagoya"), 255..265)
        )
    ),
    GERMANY(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.germany"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.berlin"), 259..269),
            CityPreset(Component.translatable("name.myshore.wd.city.hamburg"), 257..267),
            CityPreset(Component.translatable("name.myshore.wd.city.munich"), 258..268),
            CityPreset(Component.translatable("name.myshore.wd.city.cologne"), 256..266)
        )
    ),
    NORTH_KOREA(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.north_korea"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.pyongyang"), 244..254),
            CityPreset(Component.translatable("name.myshore.wd.city.hamhung"), 242..252),
            CityPreset(Component.translatable("name.myshore.wd.city.chongjin"), 241..251),
            CityPreset(Component.translatable("name.myshore.wd.city.nampo"), 240..250)
        )
    ),
    CUBA(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.cuba"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.havana"), 248..258),
            CityPreset(Component.translatable("name.myshore.wd.city.santiago_de_cuba"), 246..256),
            CityPreset(Component.translatable("name.myshore.wd.city.camaguey"), 244..254),
            CityPreset(Component.translatable("name.myshore.wd.city.holguin"), 243..253)
        )
    ),
    KAZAKHSTAN(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.kazakhstan"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.almaty"), 249..259),
            CityPreset(Component.translatable("name.myshore.wd.city.astana"), 248..258),
            CityPreset(Component.translatable("name.myshore.wd.city.shymkent"), 245..255),
            CityPreset(Component.translatable("name.myshore.wd.city.karaganda"), 244..254)
        )
    ),
    BAHAMAS(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.bahamas"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.nassau"), 250..260),
            CityPreset(Component.translatable("name.myshore.wd.city.freeport"), 247..257),
            CityPreset(Component.translatable("name.myshore.wd.city.marsh_harbour"), 244..254),
            CityPreset(Component.translatable("name.myshore.wd.city.george_town"), 243..253)
        )
    ),
    ISRAEL(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.israel"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.jerusalem"), 258..268),
            CityPreset(Component.translatable("name.myshore.wd.city.tel_aviv"), 260..270),
            CityPreset(Component.translatable("name.myshore.wd.city.haifa"), 255..265),
            CityPreset(Component.translatable("name.myshore.wd.city.beer_sheva"), 253..263)
        )
    ),
    AFGHANISTAN(
        COUNTRY_FLAG.text,
        Component.translatable("name.myshore.wd.country.afghanistan"),
        listOf(
            CityPreset(Component.translatable("name.myshore.wd.city.kabul"), 242..252),
            CityPreset(Component.translatable("name.myshore.wd.city.kandahar"), 240..250),
            CityPreset(Component.translatable("name.myshore.wd.city.herat"), 241..251),
            CityPreset(Component.translatable("name.myshore.wd.city.mazar"), 240..250)
        )
    );
}