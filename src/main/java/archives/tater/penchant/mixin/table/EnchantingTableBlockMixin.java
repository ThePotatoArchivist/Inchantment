package archives.tater.penchant.mixin.table;

import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.penchant.registry.PenchantFlag;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
    @ModifyReturnValue(
            method = "getMenuProvider",
            at = @At("RETURN")
    )
    private static MenuProvider replaceMenu(MenuProvider original, BlockState state, Level level, BlockPos pos) {
        if (!PenchantFlag.REWORKED_TABLE_MENU.isEnabled()) return original;

        var displayName = original.getDisplayName();
        var unlockedEnchantments = PenchantmentMenu.getUnlockedEnchantments(level, pos);

        return new ExtendedMenuProvider<Set<Holder<Enchantment>>>() {
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return new PenchantmentMenu(containerId, inventory, unlockedEnchantments, ContainerLevelAccess.create(level, pos));
            }

            @Override
            public Component getDisplayName() {
                return displayName;
            }

            @Override
            public Set<Holder<Enchantment>> getScreenOpeningData(ServerPlayer player) {
                return unlockedEnchantments;
            }
        };
    }
}
