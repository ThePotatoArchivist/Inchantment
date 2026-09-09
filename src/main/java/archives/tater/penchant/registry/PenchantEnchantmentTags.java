package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class PenchantEnchantmentTags {
    private PenchantEnchantmentTags() {}

    private static TagKey<Enchantment> create(String path) {
        return TagKey.create(Registries.ENCHANTMENT, Penchant.id(path));
    }

    // behavior
    public static final TagKey<Enchantment> DISABLED = create("disabled");
    public static final TagKey<Enchantment> NO_LEVELING = create("no_leveling");
    public static final TagKey<Enchantment> ON_RANDOM_LOOT_BOOKS = create("on_random_loot_books");
    public static final TagKey<Enchantment> IGNORE_GUARANTEED_DROP = create("ignore_guaranteed_drop");

    // categories
    public static final TagKey<Enchantment> UNIQUE = create("unique");
    public static final TagKey<Enchantment> RARE = create("rare");
    public static final TagKey<Enchantment> UNCOMMON = create("uncommon");
    public static final TagKey<Enchantment> COMMON = create("common");

}
