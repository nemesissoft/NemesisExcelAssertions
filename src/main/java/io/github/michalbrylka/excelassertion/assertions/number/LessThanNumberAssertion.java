package io.github.michalbrylka.excelassertion.assertions.number;

import org.assertj.core.api.AbstractDoubleAssert;

public record LessThanNumberAssertion(double threshold) implements NumberAssertion {
    @Override
    public void apply(AbstractDoubleAssert<? extends AbstractDoubleAssert<?>> assertion) {
        assertion.isLessThan(threshold);
    }

    @Override
    public String toString() {
        return "< " + threshold;
    }
}
