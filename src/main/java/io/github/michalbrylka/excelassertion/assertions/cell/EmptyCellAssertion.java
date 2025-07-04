package io.github.michalbrylka.excelassertion.assertions.cell;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.assertj.core.api.SoftAssertions;

@lombok.Getter(lombok.AccessLevel.PACKAGE)
@lombok.EqualsAndHashCode(callSuper = true)
public final class EmptyCellAssertion extends ValueCellAssertion<String, EmptyCellAssertion> {
    public EmptyCellAssertion(String cellAddress) {
        super(cellAddress);
    }

    @Override
    protected void assertOnValue(String actualValue, SoftAssertions softly) {
        softly.assertThat(actualValue)
                .as(() -> "cell at %s to be empty".formatted(getFullCellAddress()))
                .isNullOrEmpty();
    }

    @Override
    protected boolean isCellTypeSupported(CellType cellType) {return cellType == CellType.BLANK || cellType == CellType.STRING;}

    @Override
    protected String fromCell(Cell cell) {
        return cell.getCellType() == CellType.BLANK ? "" : cell.getStringCellValue().trim();
    }

    @Override
    protected String fromCellValue(CellValue cellValue) {
        return cellValue.getCellType() == CellType.BLANK ? "" : cellValue.getStringValue().trim();
    }

    @Override
    public String toString() {
        return "(Cell %s is EMPTY)%s".formatted(getFullCellAddress(), super.toString());
    }
}
