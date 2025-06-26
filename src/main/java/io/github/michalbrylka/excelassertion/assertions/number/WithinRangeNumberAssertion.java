package io.github.michalbrylka.excelassertion.assertions.number;

import org.assertj.core.api.AbstractDoubleAssert;

public record WithinRangeNumberAssertion(double from, double to, boolean exclusiveFrom,
                                         boolean exclusiveTo) implements NumberAssertion {
    public WithinRangeNumberAssertion {
        if (to < from) throw new IllegalArgumentException("to must be >= from");
    }

    public WithinRangeNumberAssertion(double from, double to) {
        this(from, to, false, false);
    }

    @Override
    public void apply(AbstractDoubleAssert<? extends AbstractDoubleAssert<?>> assertion) {
        if (exclusiveFrom) assertion.isGreaterThan(from);
        else assertion.isGreaterThanOrEqualTo(from);

        if (exclusiveTo) assertion.isLessThan(to);
        else assertion.isLessThanOrEqualTo(to);
    }

    @Override
    public String toString() {
        String left = exclusiveFrom ? "(" : "[";
        String right = exclusiveTo ? ")" : "]";
        return "∈ " + left + from + ".." + to + right;
    }
}
