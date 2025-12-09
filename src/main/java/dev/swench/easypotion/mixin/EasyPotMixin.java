package dev.swench.easypotion.mixin;

import dev.swench.easypotion.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
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
    
    private boolean switchToPotionSlot(PlayerEntity player, int targetSlot, boolean isPotionThrown) {
        PlayerInventory inventory = player.getInventory();
        int selectedSlot = inventory.selectedSlot;
        boolean isTargetSlot = selectedSlot == targetSlot;
        
        if (Config.randomize) {
            ArrayList<Integer> strongHealingSlots = new ArrayList<>();
            ArrayList<Integer> healingSlots = new ArrayList<>();
            
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inventory.getStack(i);
                if (stack.getItem() == Items.SPLASH_POTION) {
                    var potion = PotionUtil.getPotion(stack);
                    if (potion == Potions.STRONG_HEALING) {
                        strongHealingSlots.add(i);
                    } else if (potion == Potions.HEALING) {
                        healingSlots.add(i);
                    }
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
            if (stack.getItem() == Items.SPLASH_POTION) {
                var potion = PotionUtil.getPotion(stack);
                if (potion == Potions.STRONG_HEALING) {
                    if (highestStrongHealingSlot == -1) {
                        highestStrongHealingSlot = i;
                    }
                } else if (potion == Potions.HEALING && highestHealingSlot == -1) {
                    highestHealingSlot = i;
                }
            }
        }
        
        if (!isTargetSlot && !isPotionThrown && highestStrongHealingSlot != -1) {
            ItemStack currentStack = inventory.getStack(selectedSlot);
            if (currentStack.getItem() == Items.SPLASH_POTION) {
                var currentPotion = PotionUtil.getPotion(currentStack);
                if (currentPotion == Potions.STRONG_HEALING && selectedSlot == highestStrongHealingSlot) {
                    return false;
                }
            }
        }
        
        if (!isTargetSlot && !isPotionThrown && highestStrongHealingSlot == -1 && highestHealingSlot != -1) {
            ItemStack currentStack = inventory.getStack(selectedSlot);
            if (currentStack.getItem() == Items.SPLASH_POTION) {
                var currentPotion = PotionUtil.getPotion(currentStack);
                if (currentPotion == Potions.HEALING && selectedSlot == highestHealingSlot) {
                    return false;
                }
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
        
        ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
        if (mainHandStack.getItem() == Items.SPLASH_POTION) {
            var potion = PotionUtil.getPotion(mainHandStack);
            if (potion == Potions.STRONG_HEALING || potion == Potions.HEALING) {
                potionThrown = true;
            }
        }
    }
}

