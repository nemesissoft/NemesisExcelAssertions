package io.github.michalbrylka.excelassertion.assertions.text;

import org.assertj.core.api.AbstractStringAssert;

import java.util.Comparator;

public final class EqualsTextAssertion extends TextAssertion<EqualsTextAssertion> {
    final String expected;
    boolean ignoreNewLines;

    public EqualsTextAssertion(String expected, boolean ignoreCase, boolean ignoreNewLines) {
        super(ignoreCase);
        this.expected = expected;
        this.ignoreNewLines = ignoreNewLines;
    }

    public EqualsTextAssertion ignoreNewLines() {
        this.ignoreNewLines = true;
        return this;
    }

    public EqualsTextAssertion respectNewLines() {
        this.ignoreNewLines = false;
        return this;
    }

    @Override
    public String toString() {
        return "equal '%s' %s, %s".formatted(expected, ignoreCase ? "ignoring case" : "case sensitive", ignoreNewLines ? "ignoring new lines" : "respecting new lines");
    }

    @Override
    public void apply(AbstractStringAssert<? extends AbstractStringAssert<?>> assertion) {
        if (this.ignoreNewLines) {
            Comparator<String> baseComparator = (s1, s2) ->
                    normalizeNewLines(s1, this.ignoreCase).compareTo(
                            normalizeNewLines(s2, this.ignoreCase)
                    );

            assertion.usingComparator(Comparator.nullsFirst(baseComparator)).isEqualTo(this.expected);
        } else {
            if (this.ignoreCase) assertion.isEqualToIgnoringCase(this.expected);
            else assertion.isEqualTo(this.expected);
        }
    }

    private static String normalizeNewLines(String s, boolean ignoreCase) {
        if (s == null) return null;
        String withoutNewlines = s.replaceAll("\\R", " ")  // replaces any newline with a space
                .replaceAll("\\s+", " ") // collapse multiple spaces
                .trim(); // optional: remove leading/trailing spaces
        return ignoreCase ? withoutNewlines.toLowerCase() : withoutNewlines;
    }
}
