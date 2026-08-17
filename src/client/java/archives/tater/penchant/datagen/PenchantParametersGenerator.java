package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantComponents;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.holder.component.v1.provider.DataHolderComponentProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class PenchantParametersGenerator extends DataHolderComponentProvider {
    public PenchantParametersGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void generate(HolderLookup.Provider registries) {
        builder(Enchantments.AQUA_AFFINITY)
                .newPatch()
                .set(PenchantComponents.EXPERIENCE_COST, 50);
    }
}
