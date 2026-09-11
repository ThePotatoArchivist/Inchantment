package archives.tater.penchant.mixin.randomenchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(MerchantMenu.class)
public class MerchantMenuMixin {
    @WrapOperation(
            method = "quickMoveStack",
            at = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/world/inventory/MerchantMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z")
    )
    private boolean triggerPostProcess(MerchantMenu instance, ItemStack stack, int startSlot, int endSlot, boolean backwards, Operation<Boolean> original, Player player) {
        stack.getItem().onCraftedBy(stack, player);
        return original.call(instance, stack, startSlot, endSlot, backwards);
    }
}
