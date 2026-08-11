package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LootEnchantmentTagGenerator extends FabricTagsProvider<Enchantment> {

    public static final List<ResourceKey<Enchantment>> UNIQUE = List.of(
            Enchantments.BREACH, // vault
            Enchantments.WIND_BURST, // ominous vault
            Enchantments.SOUL_SPEED, // bartering/bastion
            Enchantments.SWIFT_SNEAK // ancient city
    );

    public static final List<ResourceKey<Enchantment>> RARE = List.of(
            Enchantments.FROST_WALKER, // igloo
            Enchantments.FIRE_ASPECT, // nether fortress, ruined portal
            Enchantments.FLAME, // nether fortress, ruined portal
            Enchantments.SILK_TOUCH, // mineshaft, dungeon
            Enchantments.FORTUNE, // mineshaft, dungeon
            Enchantments.LUNGE, // bastion
            Enchantments.RESPIRATION, // ocean ruins, shipwreck, buried treasure
            Enchantments.DEPTH_STRIDER, // ocean ruins, shipwreck, buried treasure
            Enchantments.CHANNELING, // ruins, buried treasure
            Enchantments.RIPTIDE, // ruins, buried treasure
            Enchantments.THORNS, // desert temple
            Enchantments.INFINITY, // jungle temple
            Enchantments.MULTISHOT // pillager outpost
    );

    public static final List<ResourceKey<Enchantment>> UNCOMMON = List.of(
            Enchantments.AQUA_AFFINITY,
            Enchantments.FEATHER_FALLING,
            Enchantments.FIRE_PROTECTION,
            Enchantments.BLAST_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION,
            Enchantments.SMITE,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.SWEEPING_EDGE,
            Enchantments.KNOCKBACK,
            Enchantments.PUNCH,
            Enchantments.DENSITY,
            Enchantments.BREACH,
            Enchantments.LOOTING,
            Enchantments.LUCK_OF_THE_SEA
    );

    public static final List<ResourceKey<Enchantment>> COMMON = List.of(
            Enchantments.EFFICIENCY,
            Enchantments.PROTECTION,
            Enchantments.SHARPNESS,
            Enchantments.UNBREAKING,
            Enchantments.POWER,
            Enchantments.PIERCING,
            Enchantments.QUICK_CHARGE,
            Enchantments.IMPALING,
            Enchantments.LOYALTY,
            Enchantments.LURE,
            Enchantments.DENSITY
    );

    private static ResourceKey<Enchantment> createKey(String namespace, String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(namespace, path));
    }

    public LootEnchantmentTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ENCHANTMENT, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(PenchantEnchantmentTags.UNIQUE)
                .addAll(UNIQUE)
                .addOptional(createKey("enderscape", "rebound"))
                .addOptional(createKey("enderscape", "transdimensional"));

        builder(PenchantEnchantmentTags.RARE)
                .addAll(RARE)
                .addOptional(createKey("veinminer-enchantment", "veinminer"))
                .addOptional(createKey("veinminer_enchantment", "veinminer"))
                .addOptional(createKey("enderscape", "stun_burst"));

        builder(PenchantEnchantmentTags.UNCOMMON)
                .addAll(UNCOMMON)
                .addOptional(createKey("farmersdelight", "backstabbing"))
                .addOptional(createKey("enderscape", "bundling"));

        builder(PenchantEnchantmentTags.COMMON)
                .addAll(COMMON)
                .addOptional(createKey("enderscape", "resonance"));

        builder(EnchantmentTags.TREASURE)
                .addTag(PenchantEnchantmentTags.RARE)
                .addTag(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.NON_TREASURE)
                .removeTag(PenchantEnchantmentTags.RARE)
                .removeTag(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .addTag(PenchantEnchantmentTags.COMMON)
                .setReplace(true);
        builder(EnchantmentTags.TRADEABLE)
                .removeTag(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .removeTag(PenchantEnchantmentTags.RARE)
                .removeTag(PenchantEnchantmentTags.UNIQUE)
                .removeTag(EnchantmentTags.CURSE);
        builder(EnchantmentTags.ON_TRADED_EQUIPMENT)
                .addTag(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .removeTag(PenchantEnchantmentTags.RARE)
                .removeTag(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.ON_RANDOM_LOOT)
                .addTag(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .addTag(PenchantEnchantmentTags.RARE)
                .removeTag(PenchantEnchantmentTags.UNIQUE);
        builder(PenchantEnchantmentTags.ON_RANDOM_LOOT_BOOKS)
                .removeTag(PenchantEnchantmentTags.COMMON)
                .removeTag(EnchantmentTags.CURSE);
        builder(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
                .addTag(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .addTag(PenchantEnchantmentTags.RARE)
                .removeTag(PenchantEnchantmentTags.UNIQUE);
    }
}
