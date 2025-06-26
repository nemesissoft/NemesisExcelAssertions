package io.github.michalbrylka.excelassertion.assertions.number;

import org.assertj.core.api.AbstractDoubleAssert;

public record GreaterThanNumberAssertion(double threshold) implements NumberAssertion {
    @Override
    public void apply(AbstractDoubleAssert<? extends AbstractDoubleAssert<?>> assertion) {
        assertion.isGreaterThan(threshold);
    }

    @Override
    public String toString() {
        return "> " + threshold;
    }
}
