package dev.swench.easypotion;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

public class ClothConfigIntegration {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(new TranslatableText("easypotion.config.title"))
            .setSavingRunnable(Config::save);
        
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("easypotion.config.category.general"));
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("easypotion.config.enabled"), Config.enabled)
            .setDefaultValue(false)
            .setTooltip(new TranslatableText("easypotion.config.enabled.tooltip"))
            .setSaveConsumer(value -> Config.enabled = value)
            .build());

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("easypotion.config.onlyOnThrow"), Config.onlyOnThrow)
                .setDefaultValue(false)
                .setTooltip(new TranslatableText("easypotion.config.onlyOnThrow.tooltip"))
                .setSaveConsumer(value -> Config.onlyOnThrow = value)
                .build());
        
        general.addEntry(entryBuilder.startIntSlider(new TranslatableText("easypotion.config.slot"), Config.slot, 1, 9)
            .setDefaultValue(3)
            .setTooltip(new TranslatableText("easypotion.config.slot.tooltip"))
            .setSaveConsumer(value -> Config.slot = value)
            .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("easypotion.config.randomize"), Config.randomize)
            .setDefaultValue(false)
            .setTooltip(new TranslatableText("easypotion.config.randomize.tooltip"))
            .setSaveConsumer(value -> Config.randomize = value)
            .build());
        
        return builder.build();
    }
}

