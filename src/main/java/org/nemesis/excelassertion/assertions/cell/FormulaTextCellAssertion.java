package org.nemesis.excelassertion.assertions.cell;

import org.apache.poi.ss.usermodel.*;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import org.nemesis.excelassertion.assertions.text.TextAssertion;

@lombok.Getter(lombok.AccessLevel.PACKAGE)
@lombok.EqualsAndHashCode(callSuper = true)
public final class FormulaTextCellAssertion extends ValueCellAssertion<String, FormulaTextCellAssertion> {
    private final @NotNull TextAssertion<?> assertion;

    public FormulaTextCellAssertion(String cellAddress, @NotNull TextAssertion<?> assertion) {
        super(cellAddress);
        this.assertion = assertion;
    }

    @Override
    protected void assertOnValue(String actualValue, SoftAssertions softly) {
        var softAssert = softly.assertThat(actualValue)
                .as(() -> "formula text at %s to %s".formatted(getFullCellAddress(), assertion.toString()));
        assertion.apply(softAssert);
    }

    @Override
    public String toString() {
        return "(Cell %s formula text is %s)%s".formatted(getFullCellAddress(), assertion, super.toString());
    }

    @Override
    protected boolean isCellTypeSupported(CellType cellType) {
        return cellType == CellType.FORMULA;
    }

    @Override
    protected String fromCell(Cell cell) {
        return cell.getCellFormula();
    }

    @Override
    protected String fromCellValue(CellValue cellValue) {
        return cellValue.getStringValue();
    }
}