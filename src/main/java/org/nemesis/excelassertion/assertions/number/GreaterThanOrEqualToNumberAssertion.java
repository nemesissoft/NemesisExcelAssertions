package org.nemesis.excelassertion.assertions.number;

import org.assertj.core.api.AbstractDoubleAssert;

public record GreaterThanOrEqualToNumberAssertion(double threshold) implements NumberAssertion {
    @Override
    public void apply(AbstractDoubleAssert<? extends AbstractDoubleAssert<?>> assertion) {
        assertion.isGreaterThanOrEqualTo(threshold);
    }

    @Override
    public String toString() {
        return ">= " + threshold;
    }
}
