package io.github.michalbrylka.excelassertion.assertions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import io.github.michalbrylka.excelassertion.assertions.cell.CellAssertion;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ExcelAssert implements AutoCloseable {
    private final Workbook workbook;
    private final SoftAssertions softly;
    private final List<CellAssertionAtSheet> assertions = new ArrayList<>();
    private Sheet sheet;
    private SheetRef<?> sheetRef;

    ExcelAssert(Workbook workbook) {
        this.softly = new SoftAssertions();
        this.workbook = workbook;
        selectSheetByIndex(0);
    }

    List<CellAssertionAtSheet> getAssertions() {return Collections.unmodifiableList(assertions);}

    private void selectSheetByIndex(int index) {
        if (index < workbook.getNumberOfSheets()) {
            sheet = workbook.getSheetAt(index);
            sheetRef = new SheetRefByIndex(index);
        } else {
            softly.fail("Cannot find sheet with index %d".formatted(index));
            sheet = null;
            sheetRef = null;
        }
    }

    private void selectSheetByName(String sheetName) {
        if (workbook.getSheet(sheetName) instanceof Sheet existingSheet) {
            sheet = existingSheet;
            sheetRef = new SheetRefByName(sheetName);
        } else {
            softly.fail("Cannot find sheet with name '%s'".formatted(sheetName));
            sheet = null;
            sheetRef = null;
        }
    }

    public ExcelAssert inSheet(int index) {
        selectSheetByIndex(index);
        return this;
    }

    public ExcelAssert inSheet(String sheetName) {
        selectSheetByName(sheetName);
        return this;
    }

    public ExcelAssert has(CellAssertion<?> cellAssertion) {
        addAssert(cellAssertion);
        return this;
    }

    public ExcelAssert have(CellAssertion<?>... cellAssertions) {
        for (var ca : cellAssertions) addAssert(ca);
        return this;
    }

    private void addAssert(CellAssertion<?> cellAssertion) {
        if (sheet == null || sheetRef == null)
            return; //assertion about not existing sheet already exist

        cellAssertion
                .withSheetName(sheet.getSheetName()) //bind sheet name for logging purposes
                .applyAssert(getCell(cellAssertion, sheet), softly);
        assertions.add(new CellAssertionAtSheet(cellAssertion, sheetRef));
    }

    private static @NotNull Cell getCell(CellAssertion<?> cellAssertion, Sheet sheet) {
        var ref = new CellReference(cellAssertion.getCellAddress());
        Row row = sheet.getRow(ref.getRow()) instanceof Row r ? r : sheet.createRow(ref.getRow());
        return row.getCell(ref.getCol()) instanceof Cell c ? c : row.createCell(ref.getCol());
    }

    @Override
    public void close() {
        try {
            workbook.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e); // wrap to avoid checked exceptions
        }

        softly.assertAll();
    }


    sealed interface SheetRef<T> permits SheetRefByName, SheetRefByIndex {
        T ref();

        @Override
        String toString();
    }

    record SheetRefByName(@NotNull String ref) implements SheetRef<String> {
        @Override
        public String toString() {return "'" + ref + "'";}
    }

    record SheetRefByIndex(@NotNull Integer ref) implements SheetRef<Integer> {
        @Override
        public String toString() {return "#" + ref;}
    }

    record CellAssertionAtSheet(@NotNull CellAssertion<?> assertion, @NotNull SheetRef<?> sheetRef) {
        @Override
        public String toString() {return sheetRef + ": " + assertion;}
    }
}