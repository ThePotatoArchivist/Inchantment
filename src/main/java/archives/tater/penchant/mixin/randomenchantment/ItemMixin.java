package archives.tater.penchant.mixin.randomenchantment;

import archives.tater.penchant.component.RandomEnchantment;
import archives.tater.penchant.registry.PenchantComponents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(
            method = "onCraftedPostProcess",
            at = @At("TAIL")
    )
    private void resolveEnchantment(ItemStack itemStack, Level level, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel && itemStack.has(PenchantComponents.RANDOM_ENCHANTMENT))
            RandomEnchantment.resolve(itemStack, serverLevel);
    }
}
