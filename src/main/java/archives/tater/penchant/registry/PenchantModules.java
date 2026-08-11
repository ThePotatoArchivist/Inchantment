package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class PenchantModules {
    public static final Identifier DURABILITY_REWORK = Penchant.id("durability_rework");
    public static final Identifier BOOKSHELF_PLACEMENT = Penchant.id("bookshelf_placement");
    public static final Identifier TABLE_REWORK = Penchant.id("table_rework");
    public static final Identifier NO_ANVIL_BOOKS = Penchant.id("no_anvil_books");
    public static final Identifier LOOT_REWORK = Penchant.id("loot_rework");
    public static final Identifier GUARANTEED_DROPS = Penchant.id("guaranteed_drops");
    public static final Identifier RANDOMIZED_LIBRARIANS = Penchant.id("randomized_librarians");

    /**
     * @deprecated Removed
     */
    @Deprecated
    public static final Identifier REDUCED_CURSES = Penchant.id("reduced_curses");

    private static void registerPack(Identifier id) {
        registerPack(id, PackActivationType.DEFAULT_ENABLED);
    }

    private static void registerPack(Identifier id, PackActivationType activationType) {
        ResourceLoader.registerBuiltinPack(
                id,
                FabricLoader.getInstance().getModContainer(Penchant.MOD_ID).orElseThrow(),
                Component.translatable(id.toLanguageKey("dataPack", "name")),
                activationType
        );
    }

    public static void init() {
        registerPack(DURABILITY_REWORK);
        registerPack(BOOKSHELF_PLACEMENT);
        registerPack(TABLE_REWORK);
        registerPack(NO_ANVIL_BOOKS);
        registerPack(LOOT_REWORK);
        registerPack(GUARANTEED_DROPS);
        registerPack(RANDOMIZED_LIBRARIANS, PackActivationType.NORMAL);
    }
}
