package archives.tater.penchant.mixin.loot;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.core.HolderSet;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;

import java.util.Optional;

@Mixin(EnchantWithLevelsFunction.class)
public class EnchantWithLevelsFunctionMixin {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ModifyArg(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;enchantItem(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/RegistryAccess;Ljava/util/Optional;)Lnet/minecraft/world/item/ItemStack;"),
            index = 4
    )
    private static Optional<HolderSet<Enchantment>> replaceLootTag(Optional<HolderSet<Enchantment>> original, @Local(argsOnly = true, name = "itemStack") ItemStack itemStack, @Local(argsOnly = true, name = "context") LootContext context) {
        if (!itemStack.is(Items.BOOK)) return original;
        if (original.flatMap(HolderSet::unwrapKey).orElse(null) != EnchantmentTags.ON_RANDOM_LOOT) return original;

        return Optional.of(context.getLevel().registryAccess().getOrThrow(PenchantEnchantmentTags.ON_RANDOM_LOOT_BOOKS));
    }
}
