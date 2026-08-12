package archives.tater.penchant.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PenchantFallbackData(
    Formula experienceCost,
    Formula bookRequirement,
    Formula progressCostFactorBase,
    Formula progressCostFactorIncrease
) implements PenchantFallbackParameters {
    public static final Codec<PenchantFallbackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Formula.CODEC.fieldOf("experience_cost").forGetter(PenchantFallbackData::experienceCost),
            Formula.CODEC.fieldOf("book_requirement").forGetter(PenchantFallbackData::bookRequirement),
            Formula.CODEC.fieldOf("progress_cost_factor_base").forGetter(PenchantFallbackData::progressCostFactorBase),
            Formula.CODEC.fieldOf("progress_cost_factor_increase").forGetter(PenchantFallbackData::progressCostFactorIncrease)
    ).apply(instance, PenchantFallbackData::new));
}
