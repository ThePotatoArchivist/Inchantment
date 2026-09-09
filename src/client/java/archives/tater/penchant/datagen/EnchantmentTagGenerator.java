package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

public class EnchantmentTagGenerator extends FabricTagsProvider<Enchantment> {

    public EnchantmentTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ENCHANTMENT, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(PenchantEnchantmentTags.DISABLED);
        builder(PenchantEnchantmentTags.NO_LEVELING);
        builder(PenchantEnchantmentTags.IGNORE_GUARANTEED_DROP);

        builder(PenchantEnchantmentTags.COMMON);
        builder(PenchantEnchantmentTags.UNCOMMON);
        builder(PenchantEnchantmentTags.RARE);
        builder(PenchantEnchantmentTags.UNIQUE);

        builder(PenchantEnchantmentTags.ON_RANDOM_LOOT_BOOKS)
                .forceAddTag(EnchantmentTags.ON_RANDOM_LOOT);

        builder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .removeTag(PenchantEnchantmentTags.DISABLED);
        builder(EnchantmentTags.TRADEABLE)
                .removeTag(PenchantEnchantmentTags.DISABLED);
        builder(EnchantmentTags.ON_RANDOM_LOOT)
                .removeTag(PenchantEnchantmentTags.DISABLED);
        builder(EnchantmentTags.ON_TRADED_EQUIPMENT)
                .removeTag(PenchantEnchantmentTags.DISABLED);
        builder(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
                .removeTag(PenchantEnchantmentTags.DISABLED);

        builder(ConventionalEnchantmentTags.HIDDEN_FROM_RECIPE_VIEWERS)
                .addTag(PenchantEnchantmentTags.DISABLED);
    }
}
