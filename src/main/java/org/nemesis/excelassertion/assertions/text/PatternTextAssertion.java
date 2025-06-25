package org.nemesis.excelassertion.assertions.text;

import org.assertj.core.api.AbstractStringAssert;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class PatternTextAssertion extends TextAssertion<PatternTextAssertion> {
    final String pattern;
    boolean dotallMode;

    /**
     * Enables dotall mode.
     *
     * <p> In dotall mode, the expression {@code .} matches any character,
     * including a line terminator.  By default, this expression does not match
     * line terminators.
     *
     * <p> Dotall mode can also be enabled via the embedded flag
     * expression&nbsp;{@code (?s)}.  (The {@code s} is a mnemonic for
     * "single-line" mode, which is what this is called in Perl.)  </p>
     */
    public PatternTextAssertion dotallMode() {
        this.dotallMode = true;
        return this;
    }

    public PatternTextAssertion noDotallMode() {
        this.dotallMode = false;
        return this;
    }

    @SuppressWarnings("ConstantValue")
    public PatternTextAssertion(@NotNull String pattern, boolean ignoreCase, boolean dotallMode) {
        super(ignoreCase);
        if (pattern == null)
            throw new IllegalArgumentException("pattern cannot be null");

        this.pattern = pattern;
        this.dotallMode = dotallMode;
    }

    @Override
    public String toString() {
        return "match '%s' %s, %s".formatted(pattern, ignoreCase ? "ignoring case" : "case sensitive", dotallMode ? "dotallMode" : "no dotallMode(default)");
    }

    @Override
    public void apply(AbstractStringAssert<?> assertion) {
        int flags = 0;
        if (this.ignoreCase) flags |= Pattern.CASE_INSENSITIVE;
        if (this.dotallMode) flags |= Pattern.DOTALL;

        //flags |= Pattern.COMMENTS;

        Pattern pattern = Pattern.compile(this.pattern, flags);
        assertion.matches(pattern);
    }
}
