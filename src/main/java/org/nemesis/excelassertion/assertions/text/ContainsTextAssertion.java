package org.nemesis.excelassertion.assertions.text;

import org.assertj.core.api.AbstractStringAssert;
import org.jetbrains.annotations.NotNull;

public final class ContainsTextAssertion extends TextAssertion<ContainsTextAssertion> {
    final String expectedSubstring;

    @SuppressWarnings("ConstantValue")
    public ContainsTextAssertion(@NotNull String expectedSubstring, boolean ignoreCase) {
        super(ignoreCase);
        if (expectedSubstring == null)
            throw new IllegalArgumentException("expectedSubstring cannot be null");
        this.expectedSubstring = expectedSubstring;
    }

    @Override
    public String toString() {
        return "contain '%s' %s".formatted(expectedSubstring, ignoreCase ? "ignoring case" : "case sensitive");
    }

    @Override
    public void apply(AbstractStringAssert<? extends AbstractStringAssert<?>> assertion) {
        if (this.ignoreCase) assertion.containsIgnoringCase(this.expectedSubstring);
        else assertion.contains(this.expectedSubstring);
    }
}
