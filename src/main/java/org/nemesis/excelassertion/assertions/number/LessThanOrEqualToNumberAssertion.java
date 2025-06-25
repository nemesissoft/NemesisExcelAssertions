package org.nemesis.excelassertion.assertions.number;

import org.assertj.core.api.AbstractDoubleAssert;

public record LessThanOrEqualToNumberAssertion(double threshold) implements NumberAssertion {
    @Override
    public void apply(AbstractDoubleAssert<? extends AbstractDoubleAssert<?>> assertion) {
        assertion.isLessThanOrEqualTo(threshold);
    }

    @Override
    public String toString() {
        return "<= " + threshold;
    }
}
