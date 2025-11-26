package archives.tater.penchant.mixin.client;

import archives.tater.penchant.EnchantmentProgress;
import archives.tater.penchant.Penchant;
import archives.tater.penchant.PenchantClient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Mixin(ItemEnchantments.class)
public class ItemEnchantmentsMixin {
    @Inject(
            method = "addToTooltip",
            at = @At("HEAD")
    )
    private void getProgress(TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter, CallbackInfo ci, @Share("progress") LocalRef<EnchantmentProgress> progress) {
        if (dataComponentGetter.get(DataComponents.STORED_ENCHANTMENTS) == null)
            progress.set(dataComponentGetter.getOrDefault(Penchant.ENCHANTMENT_PROGRESS, EnchantmentProgress.EMPTY));
    }

    @WrapOperation(
            method = "addToTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getFullname(Lnet/minecraft/core/Holder;I)Lnet/minecraft/network/chat/Component;")
    )
    private Component hideLevel(Holder<@NotNull Enchantment> holder, int i, Operation<Component> original, @Share("progress") LocalRef<EnchantmentProgress> progress, @Share("enchantment") LocalRef<Holder<Enchantment>> enchantmentShare, @Share("level") LocalIntRef level) {
        if (progress.get() == null) return Penchant.getName(holder);
        enchantmentShare.set(holder);
        level.set(i);
        return original.call(holder, i);
    }

    @WrapOperation(
            method = "addToTooltip",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V")
    )
    private <T> void addProgress(Consumer<T> instance, T text, Operation<Void> original, @Share("enchantment") LocalRef<Holder<Enchantment>> enchantment, @Share("level") LocalIntRef level, @Share("progress") LocalRef<EnchantmentProgress> progress) {
        original.call(instance, text);

        if (!PenchantClient.SHOW_PROGRESS_KEYBIND.isDownAnywhere()) return;

        if (!EnchantmentProgress.shouldShowTooltip(enchantment.get())) return;

        original.call(instance, PenchantClient.getProgressTooltip(progress.get(), enchantment.get(), level.get()));
    }

    @Inject(
            method = "addToTooltip",
            at = @At("TAIL")
    )
    private void addHint(TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter, CallbackInfo ci) {
        if (PenchantClient.SHOW_PROGRESS_KEYBIND.isDownAnywhere()) return;

        tooltipAdder.accept(PenchantClient.getProgressKeyHint());
    }
}
