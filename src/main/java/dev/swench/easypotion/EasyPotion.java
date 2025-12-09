package dev.swench.easypotion;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class EasyPotion implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Config.load();
        
        Config.configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.easypotion.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.easypotion"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Config.configKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(ClothConfigIntegration.createConfigScreen(null));
                }
            }
        });
    }
}
