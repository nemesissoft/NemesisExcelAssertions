package org.nemesis.excelassertion.assertions.number;

import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.data.Offset;

public record CloseToOffsetNumberAssertion(double expected, Offset<Double> offset) implements NumberAssertion {
    @Override
    public void apply(AbstractDoubleAssert<? extends AbstractDoubleAssert<?>> assertion) {
        assertion.isCloseTo(expected, offset);
    }

    @Override
    public String toString() {
        return "~" + expected + "±" + offset.value;
    }
}
