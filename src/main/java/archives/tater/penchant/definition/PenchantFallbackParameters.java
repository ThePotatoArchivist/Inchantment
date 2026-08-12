package archives.tater.penchant.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition;

import static java.lang.Math.max;

public interface PenchantFallbackParameters {

    Formula experienceCost();
    Formula bookRequirement();
    Formula progressCostFactorBase();
    Formula progressCostFactorIncrease();

    default PenchantmentDefinition createFallback(Enchantment enchantment) {
        return new PenchantmentDefinition(
                experienceCost().calculate(enchantment.definition(), 1),
                bookRequirement().calculate(enchantment.definition(), 0),
                new Enchantment.Cost(
                        progressCostFactorBase().calculate(enchantment.definition(), 1),
                        progressCostFactorIncrease().calculate(enchantment.definition(), 0)
                )
        );
    }

    record Formula(NumberSource source, int coefficient, int constant) {

        public static final Codec<Formula> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                NumberSource.CODEC.fieldOf("source").forGetter(Formula::source),
                Codec.INT.optionalFieldOf("coefficient", 1).forGetter(Formula::coefficient),
                Codec.INT.optionalFieldOf("constant", 0).forGetter(Formula::constant)
        ).apply(instance, Formula::new));

        public Formula(NumberSource source) {
            this(source, 1, 0);
        }

        int calculate(EnchantmentDefinition definition) {
            return coefficient * source.get(definition) + constant;
        }

        int calculate(EnchantmentDefinition definition, int min) {
            return max(min, calculate(definition));
        }
    }

    enum NumberSource implements StringRepresentable {
        WEIGHT("weight"),
        MAX_LEVEL("max_level"),
        MIN_COST_BASE("min_cost_base"),
        MIN_COST_INCREASE("min_cost_base"),
        MIN_COST_LEVEL_MAX("min_cost_level_max"),
        MAX_COST_BASE("max_cost_base"),
        MAX_COST_INCREASE("max_cost_base"),
        MAX_COST_LEVEL_MAX("max_cost_level_max"),
        ANVIL_COST("anvil_cost");

        public static final Codec<NumberSource> CODEC = StringRepresentable.fromEnum(NumberSource::values);

        private final String name;

        NumberSource(String name) {
            this.name = name;
        }

        public int get(EnchantmentDefinition definition) {
            return switch (this) {
                case WEIGHT -> definition.weight();
                case MAX_LEVEL -> definition.maxLevel();
                case MIN_COST_BASE -> definition.minCost().base();
                case MIN_COST_INCREASE -> definition.minCost().perLevelAboveFirst();
                case MIN_COST_LEVEL_MAX -> definition.minCost().calculate(definition.maxLevel());
                case MAX_COST_BASE -> definition.maxCost().base();
                case MAX_COST_INCREASE -> definition.maxCost().perLevelAboveFirst();
                case MAX_COST_LEVEL_MAX -> definition.maxCost().calculate(definition.maxLevel());
                case ANVIL_COST -> definition.anvilCost();
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
