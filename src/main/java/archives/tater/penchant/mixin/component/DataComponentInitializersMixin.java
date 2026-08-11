package archives.tater.penchant.mixin.component;

import archives.tater.penchant.component.GenericInitializerContext;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(DataComponentInitializers.class)
public class DataComponentInitializersMixin implements GenericInitializerContext {
    @Unique
    private final List<GenericInitializerEntry<?>> generalInitializers = new ArrayList<>();

    @ModifyReturnValue(
            method = "runInitializers",
            at = @At("RETURN")
    )
    private Map<ResourceKey<?>, DataComponentMap.Builder> addGenericInitializers(Map<ResourceKey<?>, DataComponentMap.Builder> original, HolderLookup.Provider context) {
        generalInitializers.forEach(entry -> {
            entry.run(original, context);
        });
        return original;
    }

    @Override
    public <T> void penchant$registerGenericInitializer(ResourceKey<? extends Registry<T>> registry, DataComponentInitializers.Initializer<T> initializer) {
        generalInitializers.add(new GenericInitializerEntry<>(registry, initializer));
    }

    private record GenericInitializerEntry<T>(ResourceKey<? extends Registry<T>> registry, DataComponentInitializers.Initializer<T> initializer) {
        public void run(Map<ResourceKey<?>, DataComponentMap.Builder> builders, HolderLookup.Provider context) {
            context.lookupOrThrow(registry).listElementIds().forEach(key -> {
                initializer.run(builders.computeIfAbsent(key, _ -> DataComponentMap.builder()), context, key);
            });
        }
    }
}
