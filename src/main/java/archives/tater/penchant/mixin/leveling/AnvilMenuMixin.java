package archives.tater.penchant.mixin.leveling;

import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.registry.PenchantComponents;
import archives.tater.penchant.registry.PenchantEnchantmentTags;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import org.jspecify.annotations.Nullable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @Inject(
            method = "createResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentsForCrafting(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;", ordinal = 0)
    )
    private void saveProgress(CallbackInfo ci, @Share("progress") LocalRef<EnchantmentProgress.@Nullable Mutable> progress, @Local(ordinal = 1) ItemStack result, @Local(ordinal = 2) ItemStack sacrifice) {
        if (!result.has(DataComponents.STORED_ENCHANTMENTS) && !sacrifice.has(DataComponents.STORED_ENCHANTMENTS))
            progress.set(result.getOrDefault(PenchantComponents.ENCHANTMENT_PROGRESS, EnchantmentProgress.EMPTY).toMutable());
    }

    @Expression("? + 1")
    @ModifyExpressionValue(
            method = "createResult",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private int noIncreasePair(int original, @Local Holder<Enchantment> enchantment) {
        return enchantment.is(PenchantEnchantmentTags.NO_LEVELING) ? original : original - 1;
    }

    @WrapOperation(
            method = "createResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments$Mutable;set(Lnet/minecraft/core/Holder;I)V")
    )
    private void sumProgress(ItemEnchantments.Mutable instance, Holder<Enchantment> enchantment, int level, Operation<Void> original, @Share("progress") LocalRef<EnchantmentProgress.@Nullable Mutable> progressRef, @Local(ordinal = 2) ItemStack sacrifice, @Local Entry<Holder<Enchantment>> entry) {
        var progress = progressRef.get();
        if (progress == null || enchantment.is(PenchantEnchantmentTags.NO_LEVELING)) {
            original.call(instance, enchantment, level);
            return;
        }
        var inputLevel = instance.getLevel(enchantment);
        var sacrificeLevel = entry.getIntValue();
        var sacrificeProgress = sacrifice.getOrDefault(PenchantComponents.ENCHANTMENT_PROGRESS, EnchantmentProgress.EMPTY).getProgress(enchantment);
        if (sacrificeLevel == inputLevel) {
            progress.addProgress(enchantment, sacrificeProgress);
        } else if (sacrificeLevel > inputLevel)
            progress.setProgress(enchantment, sacrificeProgress);

        original.call(instance, enchantment, level);
    }

    @WrapOperation(
            method = "createResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments$Mutable;toImmutable()Lnet/minecraft/world/item/enchantment/ItemEnchantments;")
    )
    private ItemEnchantments setProgress(ItemEnchantments.Mutable instance, Operation<ItemEnchantments> original, @Share("progress") LocalRef<EnchantmentProgress.@Nullable Mutable> progressRef, @Local(ordinal = 1) ItemStack result) {
        var progress = progressRef.get();
        if (progress != null && !progress.isEmpty()) {
            result.set(PenchantComponents.ENCHANTMENT_PROGRESS, progress.toImmutable());
            EnchantmentProgress.updateEnchantments(progress, instance, result);
        }
        return original.call(instance);
    }
}
