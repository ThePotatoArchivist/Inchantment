package archives.tater.penchant.datagen;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.loot.LootModification;
import archives.tater.penchant.registry.PenchantRegistries;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static net.minecraft.world.level.storage.loot.LootPool.lootPool;
import static net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem;
import static net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem;
import static net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly;

public class LootModificationGenerator extends FabricDynamicRegistryProvider {
    public LootModificationGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    private static void addInject(Entries entries, ResourceKey<LootTable> target, LootPool.Builder... inject) {
        entries.add(ResourceKey.create(PenchantRegistries.LOOT_MODIFICATION, Penchant.id(target.identifier().getNamespace() + "/" + target.identifier().getPath())), new LootModification(
                List.of(target),
                Arrays.stream(inject).map(LootPool.Builder::build).toList(),
                List.of(),
                Optional.empty()
        ));
    }

    @SafeVarargs
    private static LootPool.Builder createBooks(HolderLookup.RegistryLookup<Enchantment> registry, ResourceKey<Enchantment>... enchantments) {
        return createBooks(registry, 1, 0, enchantments);
    }

    @SafeVarargs
    private static LootPool.Builder createBooks(HolderLookup.RegistryLookup<Enchantment> registry, int emptyWeight, ResourceKey<Enchantment>... enchantments) {
        return createBooks(registry, 1, emptyWeight, enchantments);
    }

    @SafeVarargs
    private static LootPool.Builder createBooks(HolderLookup.RegistryLookup<Enchantment> registry, int singleBookWeight, int emptyWeight, ResourceKey<Enchantment>... enchantments) {
        var pool = lootPool();

        if (emptyWeight > 0)
            pool.add(emptyItem()
                    .setWeight(emptyWeight)
            );

        for (var enchantment : enchantments) {
            pool.add(lootTableItem(Items.BOOK)
                    .setWeight(singleBookWeight)
                    .apply(new SetEnchantmentsFunction.Builder()
                            .withEnchantment(registry.getOrThrow(enchantment), exactly(1))
                    )
            );
        }

        return pool;
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        var registry = registries.lookupOrThrow(Registries.ENCHANTMENT);
        addInject(entries, BuiltInLootTables.IGLOO_CHEST, createBooks(registry, 4, Enchantments.FROST_WALKER));
        addInject(entries, BuiltInLootTables.NETHER_BRIDGE, createBooks(registry, 6, Enchantments.FIRE_ASPECT, Enchantments.FLAME));
        addInject(entries, BuiltInLootTables.RUINED_PORTAL, createBooks(registry, 14, Enchantments.FIRE_ASPECT, Enchantments.FLAME));
        addInject(entries, BuiltInLootTables.ABANDONED_MINESHAFT, createBooks(registry, 8, Enchantments.SILK_TOUCH, Enchantments.FORTUNE));
        addInject(entries, BuiltInLootTables.SIMPLE_DUNGEON, createBooks(registry, 14, Enchantments.SILK_TOUCH, Enchantments.FORTUNE));
        addInject(entries, BuiltInLootTables.SHIPWRECK_TREASURE, createBooks(registry, 18, Enchantments.RESPIRATION, Enchantments.DEPTH_STRIDER));
        entries.add(ResourceKey.create(PenchantRegistries.LOOT_MODIFICATION, Penchant.id("minecraft/chests/underwater_ruin")), new LootModification(
                List.of(BuiltInLootTables.UNDERWATER_RUIN_SMALL, BuiltInLootTables.UNDERWATER_RUIN_BIG),
                Stream.of(
                        createBooks(registry, 8, Enchantments.RESPIRATION, Enchantments.DEPTH_STRIDER),
                        createBooks(registry, 18, Enchantments.CHANNELING, Enchantments.RIPTIDE)
                ).map(LootPool.Builder::build).toList(),
                List.of(),
                Optional.empty()
        ));
        addInject(entries, BuiltInLootTables.BURIED_TREASURE,
                createBooks(registry, Enchantments.CHANNELING, Enchantments.RIPTIDE),
                createBooks(registry, 8, Enchantments.RESPIRATION, Enchantments.DEPTH_STRIDER)
        );
        addInject(entries, BuiltInLootTables.JUNGLE_TEMPLE, createBooks(registry, 4, Enchantments.INFINITY));
        addInject(entries, BuiltInLootTables.DESERT_PYRAMID, createBooks(registry, 9, Enchantments.THORNS));
        addInject(entries, BuiltInLootTables.PILLAGER_OUTPOST, createBooks(registry, 4, Enchantments.MULTISHOT));
        addInject(entries, BuiltInLootTables.BASTION_OTHER, createBooks(registry, 7, Enchantments.LUNGE));
        addInject(entries, BuiltInLootTables.TRIAL_CHAMBERS_REWARD_RARE, createBooks(registry, 11, Enchantments.DENSITY, Enchantments.BREACH));
    }

    @Override
    public String getName() {
        return "Loot Modification";
    }
}
