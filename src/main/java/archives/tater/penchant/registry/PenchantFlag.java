package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public class PenchantFlag {
    public static final ResourceKey<Registry<PenchantFlag>> REGISTRY_KEY = ResourceKey.createRegistryKey(Penchant.id("flag"));
    public static final Registry<PenchantFlag> REGISTRY = FabricRegistryBuilder.create(REGISTRY_KEY).buildAndRegister();
    public static final TagKey<PenchantFlag> ENABLED = TagKey.create(REGISTRY_KEY, Penchant.id("enabled"));

    private final Identifier id;

    private PenchantFlag(Identifier id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return REGISTRY.wrapAsHolder(this).is(ENABLED);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static PenchantFlag register(Identifier id) {
        return Registry.register(
                REGISTRY,
                ResourceKey.create(REGISTRY_KEY, id),
                new PenchantFlag(id)
        );
    }

    private static PenchantFlag register(String path) {
        return register(Penchant.id(path));
    }

    public static final PenchantFlag REWORKED_TABLE_MENU = register("reworked_table_menu");
    public static final PenchantFlag LENIENT_BOOKSHELF_PLACEMENT = register("lenient_bookshelf_placement");
    public static final PenchantFlag NO_ANVIL_BOOKS = register("no_anvil_books");
    public static final PenchantFlag GUARANTEED_ENCHANTED_DROP = register("guaranteed_enchanted_drop");
    public static final PenchantFlag GUARANTEED_TRIDENT_DROP = register("guaranteed_trident_drop");
    public static final PenchantFlag ZOMBIE_SPAWN_PICKAXE = register("zombie_spawn_pickaxe");
    public static final PenchantFlag REPLACE_BOOK_LOOT_TAG = register("replace_book_loot_tag");

    public static void init() {

    }
}
