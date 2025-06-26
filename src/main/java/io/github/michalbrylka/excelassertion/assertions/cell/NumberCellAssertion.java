package io.github.michalbrylka.excelassertion.assertions.cell;

import org.apache.poi.ss.usermodel.*;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import io.github.michalbrylka.excelassertion.assertions.number.NumberAssertion;


@lombok.Getter(lombok.AccessLevel.PACKAGE)
@lombok.EqualsAndHashCode(callSuper = true)
public final class NumberCellAssertion extends ValueCellAssertion<Double, NumberCellAssertion> {
    private final @NotNull NumberAssertion assertion;

    public NumberCellAssertion(String cellAddress, @NotNull NumberAssertion assertion) {
        super(cellAddress);
        this.assertion = assertion;
    }

    @Override
    protected void assertOnValue(Double actualValue, SoftAssertions softly) {
        var softAssert = softly.assertThat(actualValue)
                .as(() -> "number at %s to %s".formatted(getFullCellAddress(), assertion.toString()));
        assertion.apply(softAssert);
    }

    @Override
    public String toString() {
        return "(Cell %s number is %s)%s".formatted(getFullCellAddress(), assertion, super.toString());
    }

    @Override
    protected boolean isCellTypeSupported(CellType cellType) {
        return cellType == CellType.NUMERIC;
    }

    @Override
    protected Double fromCell(Cell cell) {
        return cell.getNumericCellValue();
    }

    @Override
    protected Double fromCellValue(CellValue cellValue) {
        return cellValue.getNumberValue();
    }
}