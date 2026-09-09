package archives.tater.penchant.loot;

import archives.tater.penchant.registry.PenchantRegistries;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

public record LootModification(
        List<ResourceKey<LootTable>> targets,
        List<LootPool> pools,
        List<Holder<LootItemFunction>> functions,
        Optional<LootPoolPatch> modifyPools
) {
    public static final Codec<LootModification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.LOOT_TABLE).listOf(1, Integer.MAX_VALUE).fieldOf("targets").forGetter(LootModification::targets),
            LootPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(LootModification::pools),
            LootItemFunctions.CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(LootModification::functions),
            LootPoolPatch.CODEC.optionalFieldOf("modify_pools").forGetter(LootModification::modifyPools)
    ).apply(instance, LootModification::new));

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            registries.lookupOrThrow(PenchantRegistries.LOOT_MODIFICATION)
                    .filterElements(modification -> modification.matches(key))
                    .listElements().map(Holder::value)
                    .forEach(modification -> modification.apply(tableBuilder));
        });
    }

    public boolean matches(ResourceKey<LootTable> key) {
        return targets.contains(key);
    }

    public void apply(LootTable.Builder builder) {
        builder.pools(pools);
        builder.apply(functions, holder -> holder::value);
        modifyPools.ifPresent(patch -> builder.modifyPools(patch::apply));
    }

    public record LootPoolPatch(
            List<LootPoolEntryContainer> entries,
            List<Holder<LootItemCondition>> conditions,
            List<Holder<LootItemFunction>> functions
    ) {
        public static final Codec<LootPoolPatch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(LootPoolPatch::entries),
                LootItemCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(LootPoolPatch::conditions),
                LootItemFunctions.CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(LootPoolPatch::functions)
        ).apply(instance, LootPoolPatch::new));

        public void apply(LootPool.Builder builder) {
            builder.add(entries);
            builder.when(conditions, holder -> holder::value);
            builder.apply(functions, holder -> holder::value);
        }
    }
}
