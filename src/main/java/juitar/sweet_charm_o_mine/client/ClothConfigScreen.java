package juitar.sweet_charm_o_mine.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class ClothConfigScreen {
    public static Screen build(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("options.sweet_charm_o_mine.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory clientCategory = builder.getOrCreateCategory(
                Component.translatable("options.sweet_charm_o_mine.client"));

        clientCategory.addEntry(entryBuilder.startEnumSelector(
                        Component.translatable("options.sweet_charm_o_mine.sniper_zoom_mode"),
                        ClientData.ZoomMode.class,
                        ClientData.getCurrentZoomMode())
                .setEnumNameProvider(value -> Component.translatable("options.sweet_charm_o_mine.sniper_zoom_mode."
                        + value.name().toLowerCase(Locale.ROOT)))
                .setSaveConsumer(ClientConfig::setZoomMode)
                .setDefaultValue(ClientConfig.DEFAULT_ZOOM_MODE)
                .build());

        clientCategory.addEntry(entryBuilder.startEnumSelector(
                        Component.translatable("options.sweet_charm_o_mine.ammo_overlay_position"),
                        ClientConfig.OverlayPosition.class,
                        ClientConfig.getAmmoOverlayPosition())
                .setEnumNameProvider(value -> Component.translatable(
                        ((ClientConfig.OverlayPosition) value).getTranslationKey()))
                .setSaveConsumer(ClientConfig::setAmmoOverlayPosition)
                .setDefaultValue(ClientConfig.OverlayPosition.RIGHT_LOWER)
                .build());

        clientCategory.addEntry(entryBuilder.startFloatField(
                        Component.translatable("options.sweet_charm_o_mine.ammo_overlay_scale"),
                        ClientConfig.getConfigData().ammoOverlayScale)
                .setTooltip(Component.translatable("options.sweet_charm_o_mine.ammo_overlay_scale.tooltip"))
                .setSaveConsumer(ClientConfig::setAmmoOverlayScale)
                .setMin(ClientConfig.MIN_AMMO_OVERLAY_SCALE)
                .setMax(ClientConfig.MAX_AMMO_OVERLAY_SCALE)
                .setDefaultValue(1.0f)
                .build());

        builder.setSavingRunnable(ClientConfig::save);
        return builder.build();
    }
}
