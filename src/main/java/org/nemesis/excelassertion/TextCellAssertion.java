package excelAssertions;

import org.apache.poi.ss.usermodel.*;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;

@lombok.Getter(lombok.AccessLevel.PACKAGE)
@lombok.EqualsAndHashCode(callSuper = true)
public final class TextCellAssertion extends ValueCellAssertion<String, TextCellAssertion> {
    private final @NotNull TextAssertion<?> assertion;

    public TextCellAssertion(String cellAddress, @NotNull TextAssertion<?> assertion) {
        super(cellAddress);
        this.assertion = assertion;
    }

    @Override
    protected void assertOnValue(String actualValue, SoftAssertions softly) {
        var softAssert = softly.assertThat(actualValue)
                .as(() -> "text at %s to %s".formatted(getFullCellAddress(), assertion.toString()));
        assertion.apply(softAssert);
    }

    @Override
    public String toString() {
        return "(Cell %s text is %s)%s".formatted(getFullCellAddress(), assertion, super.toString());
    }

    @Override
    protected boolean isCellTypeSupported(CellType cellType) {
        return cellType == CellType.STRING;
    }

    @Override
    protected String fromCell(Cell cell) {
        return cell.getStringCellValue();
    }

    @Override
    protected String fromCellValue(CellValue cellValue) {
        return cellValue.getStringValue();
    }
}