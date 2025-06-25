package org.nemesis.excelassertion.assertions.cell;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.assertj.core.api.SoftAssertions;

@lombok.Getter(lombok.AccessLevel.PACKAGE)
@lombok.EqualsAndHashCode(callSuper = true)
public final class EmptyCellAssertion extends CellAssertion<EmptyCellAssertion> {
    public EmptyCellAssertion(String cellAddress) {
        super(cellAddress);
    }

    @Override
    protected void applyAssertCore(Cell cell, SoftAssertions softly) {
        if (cell == null ||
            cell.getCellType() == CellType.BLANK ||
            cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()
        )
            return;
        else if (cell.getCellType() == CellType.FORMULA) {
            CellValue cellValue = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator().evaluate(cell);

            if (cellValue == null ||
                cellValue.getCellType() == CellType.BLANK ||
                cellValue.getCellType() == CellType.STRING && cellValue.getStringValue().trim().isEmpty()
            )
                return;
        }

        softly.fail("Cell %s expected to be empty but was not".formatted(getFullCellAddress()));
    }

    @Override
    public String toString() {
        return "(Cell %s is EMPTY)%s".formatted(getFullCellAddress(), super.toString());
    }
}
