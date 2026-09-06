package archives.tater.penchant;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;

public class PenchantServerConfig extends WrappedConfig {

    @Comment("Which modules should be enabled by default for new worlds.")
    @Comment("Note this can still be overridden per-world and will not affect existing worlds.")
    @Comment("This config is mainly intended for modpack developers. If you are an end user, prefer adjusting modules per-world using the vanilla datapack menu.")
    public ModuleDefaults moduleDefaults = new ModuleDefaults();
    public static class ModuleDefaults implements Section {

        @Comment("Remove mending & alter unbreaking")
        public boolean durabilityRework = true;

        @Comment("Larger bookshelf radius & allow chiseled bookshelves")
        public boolean bookshelfPlacement = true;

        @Comment("Enchanting via chiseled bookshelves & remove table RNG")
        public boolean tableRework = true;

        @Comment("Prevent applying enchanted books to equipment in anvil")
        public boolean noAnvilBooks = true;

        @Comment("Reorganize where enchantments are unlocked & found")
        public boolean lootRework = true;

        @Comment("Mobs always drop enchanted equipment")
        public boolean guaranteedDrops = true;

        @Comment("Librarians sell a different book each time")
        public boolean randomizedLibrarians = false;

    }

}
