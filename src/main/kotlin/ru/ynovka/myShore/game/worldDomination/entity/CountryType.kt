package ru.ynovka.myShore.game.worldDomination.entity

import com.github.darksoulq.abyssallib.extension.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import ru.ynovka.myShore.texturepack.Glyphs.COUNTRY_FLAGS


enum class CountryType(
    val flag: TextComponent?,
    val nameTranslatable: TranslatableComponent,
    val citiesName: List<TranslatableComponent>
) {
    RUSSIA(
        COUNTRY_FLAGS["russia"]?.text,
        Component.translatable("name.myshore.wd.country.russia"),
        listOf(
            Component.translatable("name.myshore.wd.city.moscow"),
            Component.translatable("name.myshore.wd.city.spb"),
            Component.translatable("name.myshore.wd.city.novosibirsk",
                Component.translatable("name.myshore.wd.city.yekaterinburg")
            )
        )
    ),
    USA(
        COUNTRY_FLAGS["usa"]?.text,
        Component.translatable("name.myshore.wd.country.usa"),
        listOf(
            Component.translatable("name.myshore.wd.city.new_york"),
            Component.translatable("name.myshore.wd.city.la"),
            Component.translatable("name.myshore.wd.city.chicago"),
            Component.translatable("name.myshore.wd.city.houston")
        )
    ),
    JAPAN(
        COUNTRY_FLAGS["japan"]?.text,
        Component.translatable("name.myshore.wd.country.japan"),
        listOf(
            Component.translatable("name.myshore.wd.city.tokyo"),
            Component.translatable("name.myshore.wd.city.osaka"),
            Component.translatable("name.myshore.wd.city.yokohama"),
            Component.translatable("name.myshore.wd.city.nagoya"),
        )
    ),
    GERMANY(
        COUNTRY_FLAGS["germany"]?.text,
        Component.translatable("name.myshore.wd.country.germany"),
        listOf(
            Component.translatable("name.myshore.wd.city.berlin"),
            Component.translatable("name.myshore.wd.city.hamburg"),
            Component.translatable("name.myshore.wd.city.munich"),
            Component.translatable("name.myshore.wd.city.cologne")
        )
    ),
    NORTH_KOREA(
        COUNTRY_FLAGS["north_korea"]?.text,
        Component.translatable("name.myshore.wd.country.north_korea"),
        listOf(
            Component.translatable("name.myshore.wd.city.pyongyang"),
            Component.translatable("name.myshore.wd.city.hamhung"),
            Component.translatable("name.myshore.wd.city.chongjin"),
            Component.translatable("name.myshore.wd.city.nampo")
        )
    ),
    CUBA(
        COUNTRY_FLAGS["cuba"]?.text,
        Component.translatable("name.myshore.wd.country.cuba"),
        listOf(
            Component.translatable("name.myshore.wd.city.havana"),
            Component.translatable("name.myshore.wd.city.santiago_de_cuba"),
            Component.translatable("name.myshore.wd.city.camaguey"),
            Component.translatable("name.myshore.wd.city.holguin")
        )
    ),
    KAZAKHSTAN(
        COUNTRY_FLAGS["kazakhstan"]?.text,
        Component.translatable("name.myshore.wd.country.kazakhstan"),
        listOf(
            Component.translatable("name.myshore.wd.city.almaty"),
            Component.translatable("name.myshore.wd.city.astana"),
            Component.translatable("name.myshore.wd.city.shymkent"),
            Component.translatable("name.myshore.wd.city.karaganda")
        )
    ),
    BAHAMAS(
        COUNTRY_FLAGS["bahamas"]?.text,
        Component.translatable("name.myshore.wd.country.bahamas"),
        listOf(
            Component.translatable("name.myshore.wd.city.nassau"),
            Component.translatable("name.myshore.wd.city.freeport"),
            Component.translatable("name.myshore.wd.city.marsh_harbour"),
            Component.translatable("name.myshore.wd.city.george_town")
        )
    ),
    ISRAEL(
        COUNTRY_FLAGS["israel"]?.text,
        Component.translatable("name.myshore.wd.country.israel"),
        listOf(
            Component.translatable("name.myshore.wd.city.jerusalem"),
            Component.translatable("name.myshore.wd.city.tel_aviv"),
            Component.translatable("name.myshore.wd.city.haifa"),
            Component.translatable("name.myshore.wd.city.beer_sheva")
        )
    ),
    AFGHANISTAN(
        COUNTRY_FLAGS["afghanistan"]?.text,
        Component.translatable("name.myshore.wd.country.afghanistan"),
        listOf(
            Component.translatable("name.myshore.wd.city.kabul"),
            Component.translatable("name.myshore.wd.city.kandahar"),
            Component.translatable("name.myshore.wd.city.herat"),
            Component.translatable("name.myshore.wd.city.mazar")
        )
    );
}