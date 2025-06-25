package org.nemesis.excelassertion.assertions.number;

import org.assertj.core.api.AbstractDoubleAssert;

public record EqualToNumberAssertion(double expected) implements NumberAssertion {
    @Override
    public void apply(AbstractDoubleAssert<? extends AbstractDoubleAssert<?>> assertion) {
        assertion.isEqualTo(expected);
    }

    @Override
    public String toString() {
        return "== " + expected;
    }
}
