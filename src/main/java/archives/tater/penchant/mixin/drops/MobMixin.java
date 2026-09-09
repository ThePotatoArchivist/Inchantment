package archives.tater.penchant.mixin.drops;

import archives.tater.penchant.registry.PenchantEnchantmentTags;
import archives.tater.penchant.registry.PenchantFlag;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(
            method = "enchantSpawnedEquipment",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V")
    )
    private void guaranteeDrop(Mob instance, EquipmentSlot equipmentSlot, ItemStack stack, Operation<Void> original) {
        original.call(instance, equipmentSlot, stack);
        if (PenchantFlag.GUARANTEED_ENCHANTED_DROP.isEnabled() &&
                stack.getEnchantments().keySet().stream().anyMatch(enchantment -> !enchantment.is(PenchantEnchantmentTags.IGNORE_GUARANTEED_DROP)))
            instance.setDropChance(equipmentSlot, 1f);
    }
}
