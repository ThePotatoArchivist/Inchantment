package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.definition.PenchantmentFormula;
import archives.tater.penchant.definition.PenchantmentDefinition;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Consumer;

public class PenchantRegistries {

    public static final ResourceKey<Registry<PenchantmentDefinition>> PENCHANTMENT_DEFINITION = ResourceKey.createRegistryKey(Penchant.id("definition"));
    public static final ResourceKey<Registry<PenchantmentFormula>> FALLBACK_PARAMS = ResourceKey.createRegistryKey(Penchant.id("fallback_params"));

    public static void init() {
        DynamicRegistries.registerSynced(FALLBACK_PARAMS, PenchantmentFormula.CODEC);
        DynamicRegistries.registerSynced(PENCHANTMENT_DEFINITION, PenchantmentDefinition.CODEC);

        DynamicRegistrySetupCallback.EVENT.register(registryView -> {
            Consumer<Holder.Reference<Enchantment>> registerFallback = enchantment -> {
                var definitions = registryView.getOptional(PENCHANTMENT_DEFINITION).orElseThrow();
                if (definitions.containsKey(enchantment.key().identifier())) return;
                Registry.register(definitions, enchantment.key().identifier(), PenchantFallbackParams.createFallback(registryView.asRegistryAccess(), enchantment.value()));
            };

            registryView.registerEntryAdded(FALLBACK_PARAMS, (rawId, id, object) -> {
                if (PenchantFallbackParams.KEYS.stream().allMatch(registryView.getOptional(FALLBACK_PARAMS).orElseThrow()::containsKey))
                    RegistryEntryAddedCallback.allEntries(registryView.getOptional(Registries.ENCHANTMENT).orElseThrow(), registerFallback);
            });
        });
    }
}
