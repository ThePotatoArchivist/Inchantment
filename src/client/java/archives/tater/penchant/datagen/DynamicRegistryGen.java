package archives.tater.penchant.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class DynamicRegistryGen {
    @SafeVarargs
    public static FabricDataGenerator.Pack.RegistryDependentFactory<FabricDynamicRegistryProvider> dynamicRegistry(ResourceKey<? extends Registry<?>>... keys) {
        return (output, registriesFuture) -> new FabricDynamicRegistryProvider(output, registriesFuture) {
            @Override
            protected void configure(HolderLookup.Provider registries, Entries entries) {
                for (var key : keys)
                    entries.addAll(registries.lookupOrThrow(key));
            }

            @Override
            public String getName() {
                return "Dynamic Registries";
            }
        };
    }
}
