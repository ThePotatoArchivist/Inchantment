package archives.tater.penchant.mixin.client.leveling;

import archives.tater.penchant.PenchantClient;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.registry.PenchantComponents;
import archives.tater.penchant.registry.PenchantEnchantmentTags;
import archives.tater.penchant.registry.PenchantItemTags;
import archives.tater.penchant.util.PenchantmentHelper;

import net.fabricmc.fabric.api.item.v1.FabricTooltipFlag;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@Mixin(ItemEnchantments.class)
public class ItemEnchantmentsMixin {
    @Shadow
    @Final
    private Object2IntOpenHashMap<Holder<Enchantment>> enchantments;

    @Inject(
            method = "addToTooltip",
            at = @At("HEAD")
    )
    private void getProgress(TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components, CallbackInfo ci, @Share("progress") LocalRef<EnchantmentProgress> progress) {
        if (components.get(DataComponents.STORED_ENCHANTMENTS) == null && !PenchantClient.TOOLTIP_ITEM.orElse(ItemStack.EMPTY).is(PenchantItemTags.MAX_LEVEL_ENCHANTMENTS))
            progress.set(components.getOrDefault(PenchantComponents.ENCHANTMENT_PROGRESS, EnchantmentProgress.EMPTY));
    }

    @WrapOperation(
            method = "addToTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getFullname(Lnet/minecraft/core/Holder;I)Lnet/minecraft/network/chat/Component;")
    )
    private Component hideLevel(Holder<Enchantment> enchantment, int level, Operation<Component> original, @Share("progress") LocalRef<@Nullable EnchantmentProgress> progress, @Share("enchantmentShare") LocalRef<@Nullable Holder<Enchantment>> enchantmentShare, @Share("level") LocalIntRef shareLevel) {
        if (enchantment.is(PenchantEnchantmentTags.NO_LEVELING)) return original.call(enchantment, level);
        if (progress.get() == null) return PenchantmentHelper.getName(enchantment);
        enchantmentShare.set(enchantment);
        shareLevel.set(level);
        return original.call(enchantment, level);
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "addToTooltip",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V")
    )
    private <T> void addProgress(Consumer<T> instance, T text, Operation<Void> original, @Share("enchantmentShare") LocalRef<@Nullable Holder<Enchantment>> enchantmentShare, @Share("level") LocalIntRef level, @Share("progress") LocalRef<@Nullable EnchantmentProgress> progress, @Local(argsOnly = true, name = "components") DataComponentGetter components) {
        original.call(instance, text);

        var enchantment = enchantmentShare.get();
        if (enchantment == null) return;
        if (!PenchantClient.shouldShowProgress()) return;
        if (!EnchantmentProgress.shouldShowTooltip(enchantment)) return;

        instance.accept((T) PenchantClient.getProgressTooltip(
                progress.get(),
                enchantment,
                level.get(),
                components
        ));
    }

    @Inject(
            method = "addToTooltip",
            at = @At("TAIL")
    )
    private void addHint(TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components, CallbackInfo ci, @Share("progress") LocalRef<@Nullable EnchantmentProgress> progress) {
        if (progress.get() == null || enchantments.isEmpty() || (!PenchantClient.shouldShowKeyHint() && !((FabricTooltipFlag) flag).shouldDisplayAllInformation())) return;

        consumer.accept(PenchantClient.getProgressKeyHint());
    }
}
