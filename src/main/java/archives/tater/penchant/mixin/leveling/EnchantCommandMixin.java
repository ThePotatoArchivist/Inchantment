package archives.tater.penchant.mixin.leveling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.Holder;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {
    @WrapOperation(
            method = "enchant",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;enchant(Lnet/minecraft/core/Holder;I)V")
    )
    private static void bypassDefaultEnchant(ItemStack instance, Holder<Enchantment> enchantment, int level, Operation<Void> original) {
        EnchantmentHelper.updateEnchantments(instance, mutable -> mutable.upgrade(enchantment, level));
    }
}
