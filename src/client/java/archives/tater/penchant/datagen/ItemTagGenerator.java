package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantItemTags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends FabricTagsProvider.ItemTagsProvider {
    public ItemTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        builder(PenchantItemTags.MAX_LEVEL_ENCHANTMENTS)
                .forceAddTag(ItemTags.HARNESSES)
                .forceAddTag(ConventionalItemTags.HORSE_ARMORS)
                .forceAddTag(ConventionalItemTags.NAUTILUS_ARMORS)
                .addOptional(createKey("enderscape", "mirror"))
                .addOptional(createKey("enderscape", "magnia_attractor"));
    }

    private static ResourceKey<Item> createKey(String namespace, String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
    }
}
