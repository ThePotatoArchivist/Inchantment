package archives.tater.penchant.datagen;

import archives.tater.penchant.definition.PenchantmentDefinition;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.concurrent.CompletableFuture;

public class PenchantmentDefinitionGenerator extends FabricDynamicRegistryProvider {
    public PenchantmentDefinitionGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    private static void add(Entries entries, ResourceKey<Enchantment> enchantment, PenchantmentDefinition definition) {
        entries.add(PenchantmentDefinition.keyOf(enchantment), definition);
    }

    @Override
    protected void configure(HolderLookup.Provider provider, Entries entries) {
        add(entries, Enchantments.AQUA_AFFINITY, new PenchantmentDefinition(4, 8, Enchantment.constantCost(20)));
    }

    @Override
    public String getName() {
        return "Penchantment Definitions";
    }
}
