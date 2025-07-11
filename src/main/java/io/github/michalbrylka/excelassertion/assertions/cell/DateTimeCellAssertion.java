package io.github.michalbrylka.excelassertion.assertions.cell;

import org.apache.poi.ss.usermodel.*;
import org.assertj.core.api.LocalDateTimeAssert;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.TemporalUnitOffset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;


@lombok.Getter(lombok.AccessLevel.PACKAGE)
@lombok.EqualsAndHashCode(callSuper = true)
public final class DateTimeCellAssertion extends ValueCellAssertion<LocalDateTime, DateTimeCellAssertion> {
    private @Nullable LocalDateTime isBefore;
    private @Nullable LocalDateTime isAfter;
    private @Nullable LocalDateTime isEqualTo;
    private @Nullable Tolerance isEqualToWithTolerance;
    private @Nullable Integer hasYear;
    private @Nullable Integer hasMonthValue;
    private @Nullable Integer hasDayOfMonth;
    private @Nullable Integer hasHour;
    private @Nullable Integer hasMinute;
    private @Nullable Integer hasSecond;

    public DateTimeCellAssertion(String cellAddress) {
        super(cellAddress);
    }

    public DateTimeCellAssertion isBefore(@NotNull LocalDateTime dateTime) {
        this.isBefore = dateTime;
        return this;
    }

    public DateTimeCellAssertion isAfter(@NotNull LocalDateTime dateTime) {
        this.isAfter = dateTime;
        return this;
    }

    public DateTimeCellAssertion isEqualTo(@NotNull LocalDateTime dateTime) {
        this.isEqualTo = dateTime;
        return this;
    }

    public DateTimeCellAssertion isEqualToWithTolerance(@NotNull LocalDateTime dateTime, TemporalUnitOffset offset) {
        this.isEqualToWithTolerance = new Tolerance(dateTime, offset);
        return this;
    }

    public DateTimeCellAssertion hasYear(int year) {
        this.hasYear = year;
        return this;
    }

    public DateTimeCellAssertion hasMonthValue(int month) {
        this.hasMonthValue = month;
        return this;
    }

    public DateTimeCellAssertion hasDayOfMonth(int day) {
        this.hasDayOfMonth = day;
        return this;
    }

    public DateTimeCellAssertion hasHour(int hour) {
        this.hasHour = hour;
        return this;
    }

    public DateTimeCellAssertion hasMinute(int minute) {
        this.hasMinute = minute;
        return this;
    }

    public DateTimeCellAssertion hasSecond(int second) {
        this.hasSecond = second;
        return this;
    }

    @Override
    protected void assertOnValue(LocalDateTime actualValue, SoftAssertions softly) {
        LocalDateTimeAssert softAssert = softly.assertThat(actualValue)
                .as(() -> "datetime at %s".formatted(getFullCellAddress()));

        if (actualValue == null) {
            softAssert.isNotNull();
            return;
        }
        if (isBefore != null) softAssert.isBefore(isBefore);

        if (isAfter != null) softAssert.isAfter(isAfter);

        if (isEqualTo != null) softAssert.isEqualTo(isEqualTo);

        if (isEqualToWithTolerance != null)
            softAssert.isCloseTo(isEqualToWithTolerance.expected, isEqualToWithTolerance.offset);

        if (hasYear != null) softAssert.hasYear(hasYear);

        if (hasMonthValue != null) softAssert.hasMonthValue(hasMonthValue);

        if (hasDayOfMonth != null) softAssert.hasDayOfMonth(hasDayOfMonth);

        if (hasHour != null) softAssert.hasHour(hasHour);

        if (hasMinute != null) softAssert.hasMinute(hasMinute);

        if (hasSecond != null) softAssert.hasSecond(hasSecond);
    }

    @Override
    protected boolean isCellTypeSupported(CellType cellType) {
        return cellType == CellType.NUMERIC;
    }


    @Override
    protected LocalDateTime fromCell(Cell cell) {
        return cell.getLocalDateTimeCellValue();
    }

    @Override
    protected LocalDateTime fromCellValue(CellValue cellValue) {
        // CellValue only supports numeric, so treat as Excel date serial
        double excelDate = cellValue.getNumberValue();
        return DateUtil.getLocalDateTime(excelDate);
    }

    private record Tolerance(LocalDateTime expected, TemporalUnitOffset offset) {
    }
}