package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.PenchantmentDefinition;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class PenchantRegistries {

    public static final ResourceKey<Registry<PenchantmentDefinition>> PENCHANTMENT_DEFINITION = ResourceKey.createRegistryKey(Penchant.id("definition"));

    public static void init() {
        DynamicRegistries.register(PENCHANTMENT_DEFINITION, PenchantmentDefinition.CODEC);
    }
}
