package org.nemesis.excelassertion.assertions.number;

import org.assertj.core.api.AbstractDoubleAssert;

public record OutsideRangeNumberAssertion(double from, double to, boolean exclusiveFrom,
                                          boolean exclusiveTo) implements NumberAssertion {
    public OutsideRangeNumberAssertion {
        if (to < from) throw new IllegalArgumentException("to must be >= from");
    }

    public OutsideRangeNumberAssertion(double from, double to) {
        this(from, to, false, false);
    }

    @Override
    public void apply(AbstractDoubleAssert<? extends AbstractDoubleAssert<?>> assertion) {
        assertion.satisfies(actual -> {
            boolean left = exclusiveFrom ? actual <= from : actual < from;
            boolean right = exclusiveTo ? actual >= to : actual > to;
            if (!(left || right)) {
                throw new AssertionError("Expected number to be " + toString() + " but was " + actual);
            }
        });
    }

    @Override
    public String toString() {
        String left = exclusiveFrom ? "(" : "[";
        String right = exclusiveTo ? ")" : "]";
        return "∉ " + left + from + ".." + to + right;
    }
}
