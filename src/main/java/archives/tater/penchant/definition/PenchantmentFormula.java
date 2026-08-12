package archives.tater.penchant.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;

import static net.minecraft.util.Mth.clamp;

//    public PenchantmentDefinition createFallback(Enchantment enchantment) {
//        return new PenchantmentDefinition(
//                experienceCost().calculate(enchantment.definition(), 1),
//                bookRequirement().calculate(enchantment.definition(), 0),
//                new Enchantment.Cost(
//                        progressCostFactorBase().calculate(enchantment.definition(), 1),
//                        progressCostFactorIncrease().calculate(enchantment.definition(), 0)
//                )
//        );
//    }

public record PenchantmentFormula(NumberSource source, float coefficient, int constant, int min, int max) {

    public static final Codec<PenchantmentFormula> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NumberSource.CODEC.fieldOf("source").forGetter(PenchantmentFormula::source),
            Codec.FLOAT.optionalFieldOf("coefficient", 1f).forGetter(PenchantmentFormula::coefficient),
            Codec.INT.optionalFieldOf("constant", 0).forGetter(PenchantmentFormula::constant),
            Codec.INT.optionalFieldOf("min", Integer.MIN_VALUE).forGetter(PenchantmentFormula::min),
            Codec.INT.optionalFieldOf("max", Integer.MAX_VALUE).forGetter(PenchantmentFormula::max)
    ).apply(instance, PenchantmentFormula::new));

    public PenchantmentFormula(NumberSource source, int coefficient, int constant) {
        this(source, coefficient, constant, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public PenchantmentFormula(NumberSource source) {
        this(source, 1, 0);
    }

    public int calculate(int base) {
        return clamp((int) (coefficient * base) + constant, min, max);
    }

    public int calculate(int base, int min) {
        return Math.max(min, calculate(base));
    }

    public int calculate(Enchantment.EnchantmentDefinition definition) {
        return calculate(source.get(definition));
    }

    public int calculate(Enchantment.EnchantmentDefinition definition, int min) {
        return Math.max(min, calculate(definition));
    }

    public boolean isDurability() {
        return source == NumberSource.MAX_DURABILITY;
    }

    public enum NumberSource implements StringRepresentable {
        WEIGHT("weight"),
        MAX_LEVEL("max_level"),
        MIN_COST_BASE("min_cost_base"),
        MIN_COST_INCREASE("min_cost_base"),
        MIN_COST_LEVEL_MAX("min_cost_level_max"),
        MAX_COST_BASE("max_cost_base"),
        MAX_COST_INCREASE("max_cost_base"),
        MAX_COST_LEVEL_MAX("max_cost_level_max"),
        ANVIL_COST("anvil_cost"),
        MAX_DURABILITY("max_durability");

        public static final Codec<NumberSource> CODEC = StringRepresentable.fromEnum(NumberSource::values);

        private final String name;

        NumberSource(String name) {
            this.name = name;
        }

        public int get(Enchantment.EnchantmentDefinition definition) {
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
                case MAX_DURABILITY -> throw new UnsupportedOperationException();
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
