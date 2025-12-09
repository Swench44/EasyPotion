package dev.swench.easypotion;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClothConfigIntegration {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("easypotion.config.title"))
            .setSavingRunnable(Config::save);
        
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("easypotion.config.category.general"));
        
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("easypotion.config.enabled"), Config.enabled)
            .setDefaultValue(false)
            .setTooltip(Text.translatable("easypotion.config.enabled.tooltip"))
            .setSaveConsumer(value -> Config.enabled = value)
            .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("easypotion.config.onlyOnThrow"), Config.onlyOnThrow)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("easypotion.config.onlyOnThrow.tooltip"))
                .setSaveConsumer(value -> Config.onlyOnThrow = value)
                .build());
        
        general.addEntry(entryBuilder.startIntSlider(Text.translatable("easypotion.config.slot"), Config.slot, 1, 9)
            .setDefaultValue(3)
            .setTooltip(Text.translatable("easypotion.config.slot.tooltip"))
            .setSaveConsumer(value -> Config.slot = value)
            .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("easypotion.config.randomize"), Config.randomize)
            .setDefaultValue(false)
            .setTooltip(Text.translatable("easypotion.config.randomize.tooltip"))
            .setSaveConsumer(value -> Config.randomize = value)
            .build());
        
        return builder.build();
    }
}

