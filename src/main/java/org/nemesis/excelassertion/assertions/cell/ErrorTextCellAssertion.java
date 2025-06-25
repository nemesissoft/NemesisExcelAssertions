package org.nemesis.excelassertion.assertions.cell;

import org.apache.poi.ss.usermodel.*;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import org.nemesis.excelassertion.assertions.text.TextAssertion;

@lombok.Getter(lombok.AccessLevel.PACKAGE)
@lombok.EqualsAndHashCode(callSuper = true)
public final class ErrorTextCellAssertion extends ValueCellAssertion<String, ErrorTextCellAssertion> {
    private final @NotNull TextAssertion<?> assertion;

    public ErrorTextCellAssertion(String cellAddress, @NotNull TextAssertion<?> assertion) {
        super(cellAddress);
        this.assertion = assertion;
    }

    @Override
    protected void assertOnValue(String actualValue, SoftAssertions softly) {
        var softAssert = softly.assertThat(actualValue)
                .as(() -> "error text at %s to %s".formatted(getFullCellAddress(), assertion.toString()));
        assertion.apply(softAssert);
    }

    @Override
    public String toString() {
        return "(Cell %s error text is %s)%s".formatted(getFullCellAddress(), assertion, super.toString());
    }

    @Override
    protected boolean isCellTypeSupported(CellType cellType) {
        return cellType == CellType.ERROR;
    }

    @Override
    protected String fromCell(Cell cell) {
        return FormulaError.forInt(cell.getErrorCellValue()).getString();
    }

    @Override
    protected String fromCellValue(CellValue cellValue) {
        return FormulaError.forInt(cellValue.getErrorValue()).getString();
    }
}