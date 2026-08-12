package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.definition.PenchantmentDefinition;
import archives.tater.penchant.definition.PenchantmentFormula;
import archives.tater.penchant.definition.PenchantmentFormula.NumberSource;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Set;

public interface PenchantFallbackParams {
    private static ResourceKey<PenchantmentFormula> create(String path) {
        return ResourceKey.create(PenchantRegistries.FALLBACK_PARAMS, Penchant.id(path));
    }

    ResourceKey<PenchantmentFormula> EXPERIENCE_COST = create("experience_cost");
    ResourceKey<PenchantmentFormula> BOOK_REQUIREMENT = create("book_requirement");
    ResourceKey<PenchantmentFormula> PROGRESS_COST_FACTOR_BASE = create("progress_cost_factor_base");
    ResourceKey<PenchantmentFormula> PROGRESS_COST_FACTOR_INCREASE = create("progress_cost_factor_increase");
    ResourceKey<PenchantmentFormula> DURABILITY_PROGRESS_COST_FACTOR = create("durability_progress_cost_factor");

    Set<ResourceKey<PenchantmentFormula>> KEYS = Set.of(
            EXPERIENCE_COST,
            BOOK_REQUIREMENT,
            PROGRESS_COST_FACTOR_BASE,
            PROGRESS_COST_FACTOR_INCREASE,
            DURABILITY_PROGRESS_COST_FACTOR
    );

    static void bootstrap(BootstrapContext<PenchantmentFormula> context) {
        context.register(EXPERIENCE_COST, new PenchantmentFormula(NumberSource.ANVIL_COST));
        context.register(BOOK_REQUIREMENT, new PenchantmentFormula(NumberSource.MIN_COST_BASE, 2, -5));
        context.register(PROGRESS_COST_FACTOR_BASE, new PenchantmentFormula(NumberSource.MAX_COST_BASE));
        context.register(PROGRESS_COST_FACTOR_INCREASE, new PenchantmentFormula(NumberSource.MAX_COST_INCREASE));
        context.register(DURABILITY_PROGRESS_COST_FACTOR, new PenchantmentFormula(NumberSource.MAX_DURABILITY, 0.01f, 0, 1, 8));
    }

    static int calculate(HolderLookup.Provider registries, ResourceKey<PenchantmentFormula> key, Enchantment enchantment, int min) {
        var formula = registries.getOrThrow(key);
        if (formula.value().isDurability()) throw new UnsupportedOperationException("Fallback param " + key.identifier() + " should not have source max_durability");
        return formula.value().calculate(enchantment.definition(), min);
    }

    static int calculate(HolderLookup.Provider registries, ResourceKey<PenchantmentFormula> key, int maxDurability) {
        var formula = registries.getOrThrow(key);
        if (!formula.value().isDurability()) throw new UnsupportedOperationException("Fallback param " + key.identifier() + " should have source max_durability");
        return formula.value().calculate(maxDurability, 1);
    }

    static int calculate(HolderLookup.Provider registries, int maxDurability) {
        return calculate(registries, DURABILITY_PROGRESS_COST_FACTOR, maxDurability);
    }

    static PenchantmentDefinition createFallback(HolderLookup.Provider registries, Enchantment enchantment) {
        return new PenchantmentDefinition(
                calculate(registries, EXPERIENCE_COST, enchantment, 1),
                calculate(registries, BOOK_REQUIREMENT, enchantment, 0),
                new Enchantment.Cost(
                        calculate(registries, PROGRESS_COST_FACTOR_BASE, enchantment, 1),
                        calculate(registries, PROGRESS_COST_FACTOR_INCREASE, enchantment, 0)
                )
        );
    }
}
