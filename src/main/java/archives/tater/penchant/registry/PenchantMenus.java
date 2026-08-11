package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.menu.PenchantmentMenu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Set;

public class PenchantMenus {
    public static final ExtendedMenuType<PenchantmentMenu, Set<Holder<Enchantment>>> PENCHANTMENT_MENU = Registry.register(
            BuiltInRegistries.MENU,
            Penchant.id("penchantment"),
            new ExtendedMenuType<>(PenchantmentMenu::new, ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT).apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf))
    );

    public static void init() {

    }
}
