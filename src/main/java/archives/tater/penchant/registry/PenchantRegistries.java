package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.PenchantmentDefinition;
import archives.tater.penchant.loot.LootModification;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class PenchantRegistries {

    public static final ResourceKey<Registry<PenchantmentDefinition>> PENCHANTMENT_DEFINITION = ResourceKey.createRegistryKey(Penchant.id("definition"));
    public static final ResourceKey<Registry<LootModification>> LOOT_MODIFICATION = ResourceKey.createRegistryKey(Penchant.id("loot_modification"));

    public static void init() {
        DynamicRegistries.registerSynced(PENCHANTMENT_DEFINITION, PenchantmentDefinition.CODEC);
        DynamicRegistries.register(LOOT_MODIFICATION, LootModification.CODEC);
    }
}
