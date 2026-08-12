package archives.tater.penchant.definition;

import folk.sisby.kaleido.api.WrappedConfig;

public class PenchantFallbackConfig extends WrappedConfig implements PenchantFallbackParameters {

    public FormulaConfig experienceCost = new FormulaConfig(NumberSource.ANVIL_COST);
    public FormulaConfig bookRequirement = new FormulaConfig(NumberSource.MIN_COST_BASE, 2, -5);
    public FormulaConfig progressCostFactorBase = new FormulaConfig(NumberSource.MAX_COST_BASE);
    public FormulaConfig progressCostFactorIncrease = new FormulaConfig(NumberSource.MAX_COST_INCREASE);

    public static class FormulaConfig implements Section {

        public NumberSource source;
        public int coefficient;
        public int constant;

        public FormulaConfig(NumberSource source) {
            this(source, 1, 0);
        }

        public FormulaConfig(NumberSource source, int coefficient, int constant) {
            this.source = source;
            this.coefficient = coefficient;
            this.constant = constant;
        }

        public Formula toFormula() {
            return new Formula(source, coefficient, constant);
        }
    }

    @Override
    public Formula experienceCost() {
        return experienceCost.toFormula();
    }

    @Override
    public Formula bookRequirement() {
        return bookRequirement.toFormula();
    }

    @Override
    public Formula progressCostFactorBase() {
        return progressCostFactorBase.toFormula();
    }

    @Override
    public Formula progressCostFactorIncrease() {
        return progressCostFactorIncrease.toFormula();
    }
}
