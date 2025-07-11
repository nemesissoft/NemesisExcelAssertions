package io.github.michalbrylka.excelassertion.assertions.cell;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.TemporalUnitOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class DateTimeCellAssertionTest {

    static Stream<TestCase> provideTestCases() {
        LocalDateTime base = LocalDateTime.of(2024, 6, 1, 12, 30, 15);
        return Stream.of(
                new TestCase("isBefore passes", base.minusDays(1), a -> a.isBefore(base)),
                new TestCase("isAfter passes", base.plusDays(1), a -> a.isAfter(base)),
                new TestCase("isEqualTo passes", base, a -> a.isEqualTo(base)),
                new TestCase("isEqualToWithTolerance passes", base.plusSeconds(5),
                        a -> a.isEqualToWithTolerance(base, within(10, ChronoUnit.SECONDS))),
                new TestCase("hasYear passes", base, a -> a.hasYear(2024)),
                new TestCase("hasMonthValue passes", base, a -> a.hasMonthValue(6)),
                new TestCase("hasDayOfMonth passes", base, a -> a.hasDayOfMonth(1)),
                new TestCase("hasHour passes", base, a -> a.hasHour(12)),
                new TestCase("hasMinute passes", base, a -> a.hasMinute(30)),
                new TestCase("hasSecond passes", base, a -> a.hasSecond(15))
        );
    }

    /*@ParameterizedTest(name = "{0}")
    @MethodSource("provideTestCases")
    void assertOnValue_passes(String name, LocalDateTime actual, AssertionConfigurer configurer) {
        DateTimeCellAssertion assertion = new DateTimeCellAssertion("A1");
        configurer.configure(assertion);
        SoftAssertions softly = new SoftAssertions();
        assertion.assertOnValue(actual, softly);
        softly.assertAll();
    }*/

    @Test
    @DisplayName("assertOnValue fails on null actualValue")
    void assertOnValue_failsOnNull() {
        DateTimeCellAssertion assertion = new DateTimeCellAssertion("A1").isBefore(LocalDateTime.now());
        SoftAssertions softly = new SoftAssertions();
        assertion.assertOnValue(null, softly);
        assertThatThrownBy(softly::assertAll)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("datetime at");
    }

    interface AssertionConfigurer {
        void configure(DateTimeCellAssertion assertion);
    }

    record TestCase(String name, LocalDateTime actual, AssertionConfigurer configurer) {}
}