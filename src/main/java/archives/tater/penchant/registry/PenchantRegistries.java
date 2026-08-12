package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.definition.PenchantmentFormula;
import archives.tater.penchant.definition.PenchantmentDefinition;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public class PenchantRegistries {

    public static final ResourceKey<Registry<PenchantmentDefinition>> PENCHANTMENT_DEFINITION = ResourceKey.createRegistryKey(Penchant.id("definition"));
    public static final ResourceKey<Registry<PenchantmentFormula>> FALLBACK_PARAMS = ResourceKey.createRegistryKey(Penchant.id("fallback_params"));

    public static void init() {
        DynamicRegistries.registerSynced(FALLBACK_PARAMS, PenchantmentFormula.CODEC);
        DynamicRegistries.registerSynced(PENCHANTMENT_DEFINITION, PenchantmentDefinition.CODEC);

        DynamicRegistrySetupCallback.EVENT.register(registryView -> {
            registryView.registerEntryAdded(Registries.ENCHANTMENT, (rawId, id, enchantment) -> {
                var definitions = registryView.getOptional(PENCHANTMENT_DEFINITION).orElseThrow();
                if (definitions.containsKey(id)) return;
                Registry.register(definitions, id, PenchantFallbackParams.createFallback(registryView.asRegistryAccess(), enchantment));
            });
        });
    }
}
