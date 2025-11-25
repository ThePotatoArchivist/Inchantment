package archives.tater.inchantment.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {
    @WrapOperation(
            method = {
                    "method_59969",
                    "method_59972",
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I")
    )
    private static int onlyOneBookLevel(Enchantment instance, Operation<Integer> original) {
        return instance.getMinLevel();
    }
}
