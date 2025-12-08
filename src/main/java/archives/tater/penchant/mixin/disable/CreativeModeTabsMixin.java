package archives.tater.penchant.mixin.disable;

import archives.tater.penchant.PenchantEnchantmentTags;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.Holder;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.stream.Stream;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {
    @ModifyExpressionValue(
            method = {
                    "generateEnchantmentBookTypesAllLevels",
                    "generateEnchantmentBookTypesOnlyMaxLevel"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;")
    )
    private static Stream<Holder.Reference<Enchantment>> disableEnchantment(Stream<Holder.Reference<Enchantment>> original) {
        return original.filter(enchantment -> !enchantment.is(PenchantEnchantmentTags.DISABLED));
    }
}
