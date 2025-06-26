package io.github.michalbrylka.excelassertion.assertions.number;

import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.data.Percentage;

public record CloseToPercentNumberAssertion(double expected, Percentage percentage) implements NumberAssertion {
    @Override
    public void apply(AbstractDoubleAssert<? extends AbstractDoubleAssert<?>> assertion) {
        assertion.isCloseTo(expected, percentage);
    }

    @Override
    public String toString() {
        return "~" + expected + "±" + percentage.value + "%";
    }
}
