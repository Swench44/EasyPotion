package dev.swench.easypotion.mixin;

import dev.swench.easypotion.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Random;

@Mixin(MinecraftClient.class)
public class EasyPotMixin {
    
    private boolean potionThrown = false;
    private boolean shouldCheck = true;
    private final Random random = new Random();
    
    private boolean isHealingPotion(ItemStack stack) {
        if (!(stack.getItem() == Items.SPLASH_POTION)) {
            return false;
        }
        
        var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) {
            return false;
        }
        
        for (StatusEffectInstance effectInstance : potionContents.getEffects()) {
            if (effectInstance.getEffectType() == StatusEffects.INSTANT_HEALTH && effectInstance.getAmplifier() == 0) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isStrongHealingPotion(ItemStack stack) {
        if (!(stack.getItem() == Items.SPLASH_POTION)) {
            return false;
        }
        
        var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) {
            return false;
        }
        
        for (StatusEffectInstance effectInstance : potionContents.getEffects()) {
            if (effectInstance.getEffectType() == StatusEffects.INSTANT_HEALTH && effectInstance.getAmplifier() >= 1) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean switchToPotionSlot(PlayerEntity player, int targetSlot, boolean isPotionThrown) {
        PlayerInventory inventory = player.getInventory();
        int selectedSlot = inventory.selectedSlot;
        boolean isTargetSlot = selectedSlot == targetSlot;
        
        if (Config.randomize) {
            ArrayList<Integer> strongHealingSlots = new ArrayList<>();
            ArrayList<Integer> healingSlots = new ArrayList<>();
            
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inventory.getStack(i);
                if (isStrongHealingPotion(stack)) {
                    strongHealingSlots.add(i);
                } else if (isHealingPotion(stack)) {
                    healingSlots.add(i);
                }
            }
            
            if (strongHealingSlots.isEmpty() && healingSlots.isEmpty()) {
                return false;
            }
            
            ArrayList<Integer> slotsToChoose = !strongHealingSlots.isEmpty() ? strongHealingSlots : healingSlots;
            int randomSlot = slotsToChoose.get(random.nextInt(slotsToChoose.size()));
            
            if (isTargetSlot && !isPotionThrown && randomSlot == selectedSlot) {
                return false;
            }
            
            inventory.selectedSlot = randomSlot;
            return true;
        }
        
        int highestStrongHealingSlot = -1;
        int highestHealingSlot = -1;
        
        for (int i = 8; i >= 0; i--) {
            ItemStack stack = inventory.getStack(i);
            if (isStrongHealingPotion(stack)) {
                if (highestStrongHealingSlot == -1) {
                    highestStrongHealingSlot = i;
                }
            } else if (isHealingPotion(stack) && highestHealingSlot == -1) {
                highestHealingSlot = i;
            }
        }
        
        if (!isTargetSlot && !isPotionThrown && highestStrongHealingSlot != -1) {
            ItemStack currentStack = inventory.getStack(selectedSlot);
            if (isStrongHealingPotion(currentStack) && selectedSlot == highestStrongHealingSlot) {
                return false;
            }
        }
        
        if (!isTargetSlot && !isPotionThrown && highestStrongHealingSlot == -1 && highestHealingSlot != -1) {
            ItemStack currentStack = inventory.getStack(selectedSlot);
            if (isHealingPotion(currentStack) && selectedSlot == highestHealingSlot) {
                return false;
            }
        }
        
        if (highestStrongHealingSlot != -1) {
            inventory.selectedSlot = highestStrongHealingSlot;
            return true;
        }
        
        if (highestHealingSlot != -1) {
            inventory.selectedSlot = highestHealingSlot;
            return true;
        }
        
        return false;
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!Config.enabled) {
            return;
        }
        
        MinecraftClient client = (MinecraftClient) (Object) this;
        PlayerEntity player = client.player;
        
        if (player == null || client.world == null) {
            return;
        }
        
        PlayerInventory inventory = player.getInventory();
        int selectedSlot = inventory.selectedSlot;
        int targetSlot = Config.slot - 1;
        
        if (potionThrown) {
            if (switchToPotionSlot(player, targetSlot, true)) {
                potionThrown = false;
            }
        }
        
        if (!Config.onlyOnThrow) {
            if (selectedSlot == targetSlot) {
                if (shouldCheck) {
                    if (!switchToPotionSlot(player, targetSlot, false)) {
                        shouldCheck = false;
                    }
                }
            } else {
                shouldCheck = true;
            }
        }
    }
    
    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void onItemUse(CallbackInfo ci) {
        if (!Config.enabled) {
            return;
        }
        
        MinecraftClient client = (MinecraftClient) (Object) this;
        PlayerEntity player = client.player;
        
        if (player == null) {
            return;
        }
        
        ItemStack mainHandStack = player.getMainHandStack();
        if (isHealingPotion(mainHandStack) || isStrongHealingPotion(mainHandStack)) {
            potionThrown = true;
        }
    }
}

