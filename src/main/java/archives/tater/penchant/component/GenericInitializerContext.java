package archives.tater.penchant.component;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.resources.ResourceKey;

public interface GenericInitializerContext {
    <T> void penchant$registerGenericInitializer(ResourceKey<? extends Registry<T>> registry, DataComponentInitializers.Initializer<T> initializer);
}
