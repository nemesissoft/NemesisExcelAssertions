package io.github.michalbrylka.excelassertion.assertions;

import io.github.michalbrylka.excelassertion.assertions.cell.CellAssertion;
import io.github.michalbrylka.excelassertion.io.*;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Named;

import java.util.stream.Stream;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static io.github.michalbrylka.excelassertion.assertions.ExcelAssertionBuilder.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ExcelAssertionBuilderTest {
    private ExcelAssert assertThatExcelFile;
    private static File exampleFile;

    @Test
    void testAllAssertionsPass() {
        assertThatExcelFile
                //check empty
                .inSheet(1).has(
                        cellAt("B1").empty()
                )
                //numbers
                .inSheet("Numbers").have(
                        cellAt("A1").withNumber(equalTo(2.0)).withFormat("0.00"),
                        cellAt("A2").withNumber(greaterThan(33)).withFormat("0.0000%"),
                        cellAt("A3").withNumber(greaterThanOrEqualTo(10)).withFormat("0.00"),
                        cellAt("A4").withNumber(lessThan(0.0000001)).withFormat("0.00000000"),
                        cellAt("A5").withNumber(lessThanOrEqualTo(-9999999)).withFormat("#,##0"),
                        cellAt("A6").withNumber(closeTo(1.4142, Offset.offset(0.0001))).withFormat("0.0000"),
                        cellAt("A7").withNumber(closeTo(3.1105, Percentage.withPercentage(1))).withFormat("0.0000"),

                        cellAt("A1").withNumber(withinRange(1.99, 2.01)),
                        cellAt("A1").withNumber(withinRange(1.99999, 2.00001, true, true)),
                        cellAt("A1").withNumber(outsideRange(2.01, 2.02)),
                        cellAt("A1").withNumber(outsideRange(2.00, 2.02, true, true))
                )
                //formats
                .inSheet("Numbers").have(
                        cellAt("A1").exists().withFormat("0.00"),
                        cellAt("A2").exists().withFormat(equalTo("0.0000%")),
                        cellAt("A4").exists().withFormat(containing("00000000")),
                        cellAt("A5").exists().withFormat(matching(".*##\\d"))
                )
                //formulas
                .inSheet("Strings").have(
                        cellAt("A2").withFormulaText(containing("&\"World\"").caseSensitive()),
                        cellAt("A3").withFormulaText(equalTo("""
                                TEXT(123456,"##0° 00' 00''")""").ignoreCase()),
                        cellAt("A5").withFormulaText(containing("char").ignoreCase()),
                        cellAt("A6").withFormulaText(matching(".*[1-3]{3}.*").dotallMode())
                )
                //texts
                .inSheet("Strings").have(
                        cellAt("A1").withText(containing("report").ignoreCase()),
                        cellAt("A2").withText(equalTo("Hello World").caseSensitive()),
                        cellAt("A3").withText("123456"),
                        cellAt("A4").withText(equalTo("\"\"")),
                        cellAt("A5").withText(equalTo("Line1\n\nline2").ignoreCase().ignoreNewLines()),
                        cellAt("A5").withText(matching("Line1.*line2").ignoreCase().dotallMode()),
                        cellAt("A6").withText(matching("^[1-6]{6}$")),
                        cellAt("A7").withText(matching("""
                                ^=sUm\\(\\d+,\\d+\\)$""").ignoreCase())
                )
                //errors
                .inSheet("Errors").have(
                        cellAt("A1").withErrorText(containing("div/0").ignoreCase()),
                        cellAt("A2").withErrorText(equalTo("#N/A").caseSensitive()),
                        cellAt("A3").withErrorText(equalTo("#NUM!\n").ignoreNewLines()),
                        cellAt("A4").withErrorText(matching("^#VaL\\w[a-z]!$").ignoreCase())
                )
                //booleans
                .inSheet("Booleans").have(
                        cellAt("A1").withBoolean(ofTrue()),
                        cellAt("A2").withBoolean(equalTo(true)),
                        cellAt("A3").withBoolean(ofFalse())
                )
                //comments
                .inSheet("Comments").have(
                        cellAt("A1").exists().withComment("FORMAT"),
                        cellAt("A1").exists().withComment(equalTo("format").ignoreCase()),
                        cellAt("A5").exists().withComment(containing("MpT").ignoreCase()),
                        cellAt("A9").exists().withComment(matching("\\waL[ue]{2}").ignoreCase())
                )
        ;
        assertThatCode(() -> assertThatExcelFile.close())
                .doesNotThrowAnyException();

        assertThat(assertThatExcelFile.getAssertions()).hasSize(39);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("negativeAssertionCases")
    void testAllAssertionsNegative(Object sheetId, CellAssertion<?> cellAssertion, String expectedErrorMessagePart) {
        if (sheetId instanceof Integer)
            assertThatExcelFile.inSheet((Integer) sheetId);
        else assertThatExcelFile.inSheet((String) sheetId);

        assertThatExcelFile.has(cellAssertion);

        assertThatThrownBy(() -> assertThatExcelFile.close())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(expectedErrorMessagePart);
    }

    static Stream<Arguments> negativeAssertionCases() {
        return Stream.of(
                // Empty cell
                Arguments.of(
                        Named.of("A1 not empty in sheet 1", 1),
                        cellAt("A1").empty(),
                        "dupa"
                ),
                Arguments.of(
                        Named.of("B1 is empty in sheet 1", 1),
                        cellAt("B1").withNumber(equalTo(1.0)),
                        "dupa"
                ),
                // Numbers
                Arguments.of(
                        Named.of("A1 wrong number", "Numbers"),
                        cellAt("A1").withNumber(equalTo(3.0)).withFormat("0.00"),
                        "expected: 3.0"
                ),
                Arguments.of(
                        Named.of("A2 not greater than 34", "Numbers"),
                        cellAt("A2").withNumber(greaterThan(34)).withFormat("0.0000%"),
                        "to be greater than:"
                ),
                Arguments.of(
                        Named.of("A3 not greater than or equal to Double.MAX_VALUE", "Numbers"),
                        cellAt("A3").withNumber(greaterThanOrEqualTo(Double.MAX_VALUE)).withFormat("0.00"),
                        "to be greater than or equal to:"
                ),
                Arguments.of(
                        Named.of("A4 not less than 0", "Numbers"),
                        cellAt("A4").withNumber(lessThan(0)).withFormat("0.00000000"),
                        "to be less than:"
                ),
                Arguments.of(
                        Named.of("A5 not less than or equal to -10000000", "Numbers"),
                        cellAt("A5").withNumber(lessThanOrEqualTo(-10000000)).withFormat("#,##0"),
                        "to be less than or equal to:"
                ),
                Arguments.of(
                        Named.of("A6 not close to 1.5", "Numbers"),
                        cellAt("A6").withNumber(closeTo(1.5, Offset.offset(0.0001))).withFormat("0.0000"),
                        "to be close to:"
                ),
                Arguments.of(
                        Named.of("A7 not close to 3.1105 (+- 1%)", "Numbers"),
                        cellAt("A7").withNumber(closeTo(3.1104, Percentage.withPercentage(1))).withFormat("0.0000"),
                        "to be close to:"
                ),
                Arguments.of(
                        Named.of("A1 not within range [0..1.99]", "Numbers"),
                        cellAt("A1").withNumber(withinRange(0, 1.99)),
                        "to be less than or equal to:"
                ),
                Arguments.of(
                        Named.of("A1 not within exclusive range (2.0..3)", "Numbers"),
                        cellAt("A1").withNumber(withinRange(2.0, 3, true, true)),
                        "to be greater than:"
                ),
                Arguments.of(
                        Named.of("A1 not outside range [1..3]", "Numbers"),
                        cellAt("A1").withNumber(outsideRange(1, 3)),
                        "Expected number to be ∉ [1.0..3.0] but was 2.0"
                ),
                Arguments.of(
                        Named.of("A1 not outside exclusive range (1..2]", "Numbers"),
                        cellAt("A1").withNumber(outsideRange(1, 2, true, false)),
                        "Expected number to be ∉ (1.0..2.0] but was 2.0"
                ),
                // Formats
                Arguments.of(
                        Named.of("A1 wrong format", "Numbers"),
                        cellAt("A1").exists().withFormat("wrong-format"),
                        "expected: \"wrong-format\""
                ),
                Arguments.of(
                        Named.of("A2 wrong format (equalTo)", "Numbers"),
                        cellAt("A2").exists().withFormat(equalTo("0.0000")),
                        "but was: \"0.0000%\""
                ),
                Arguments.of(
                        Named.of("A4 format not containing 'xyz'", "Numbers"),
                        cellAt("A4").exists().withFormat(containing("xyz")),
                        "to contain:"
                ),
                Arguments.of(
                        Named.of("A5 format not matching 'abc.*'", "Numbers"),
                        cellAt("A5").exists().withFormat(matching("abc.*")),
                        "to match pattern:"
                ),
                // Formulas
                Arguments.of(
                        Named.of("A2 formula not containing 'Helloo'", "Strings"),
                        cellAt("A2").withFormulaText(containing("Helloo")),
                        "to contain:"
                ),
                Arguments.of(
                        Named.of("A2 formula not containing 'hello' CS", "Strings"),
                        cellAt("A2").withFormulaText(containing("hello").caseSensitive()),
                        "to contain:"
                ),
                Arguments.of(
                        Named.of("A3 formula not equal to 'wrong'", "Strings"),
                        cellAt("A3").withFormulaText(equalTo("wrong")),
                        "expected: \"wrong\""
                ),
                Arguments.of(
                        Named.of("A5 formula not containing 'line' CS", "Strings"),
                        cellAt("A5").withFormulaText(containing("line").caseSensitive()),
                        "to contain:"
                ),
                Arguments.of(
                        Named.of("A6 formula not matching '\\d{4}'", "Strings"),
                        cellAt("A6").withFormulaText(matching("\\d{4}")),
                        "to match pattern:"
                ),
                // Texts
                Arguments.of(
                        Named.of("A1 text not containing 'Quarterly' CS", "Strings"),
                        cellAt("A1").withText(containing("quarterly").caseSensitive()),
                        "to contain:"
                ),
                Arguments.of(
                        Named.of("A2 text not equal to 'Hello world' CS", "Strings"),
                        cellAt("A2").withText("Hello world"),
                        "expected: \"Hello world\""
                ),
                Arguments.of(
                        Named.of("A3 text not containing '14'", "Strings"),
                        cellAt("A3").withText(containing("14")),
                        "to contain:"
                ),
                Arguments.of(
                        Named.of("A4 text not equal to ''", "Strings"),
                        cellAt("A4").withText(equalTo("''")),
                        "expected: \"''\""
                ),
                Arguments.of(
                        Named.of("A5 text not equal to \"Line1\\n\\nline2\" respecting new lines", "Strings"),
                        cellAt("A5").withText(equalTo("Line1\n\nline2").ignoreCase().respectNewLines()),
                        "but was: \"Line1"
                ),
                Arguments.of(
                        Named.of("A5 text not matching 'Line1.*line2' no dotall", "Strings"),
                        cellAt("A5").withText(matching("Line1.*line2").ignoreCase().noDotallMode()),
                        "to match pattern:"
                ),
                Arguments.of(
                        Named.of("A6 text not matching '^[1-5]{6}$'", "Strings"),
                        cellAt("A6").withText(matching("^[1-5]{6}$")),
                        "to match pattern:"
                ),
                Arguments.of(
                        Named.of("A7 text not matching '...sUm...' CS", "Strings"),
                        cellAt("A7").withText(matching("""
                                ^=sUm\\(\\d+,\\d+\\)$""").caseSensitive()),
                        "to match pattern:"
                ),
                // Errors
                Arguments.of(
                        Named.of("A1 error not containing 'div' CS", "Errors"),
                        cellAt("A1").withErrorText(containing("div").caseSensitive()),
                        "to contain:"
                ),
                Arguments.of(
                        Named.of("A2 error not equal to '#REF!'", "Errors"),
                        cellAt("A2").withErrorText(equalTo("#REF!").caseSensitive()),
                        "expected: \"#REF!\""
                ),
                Arguments.of(
                        Named.of("A3 error not equal to '#NUM!\\r' respecting new lines", "Errors"),
                        cellAt("A3").withErrorText(equalTo("#NUM!\r").respectNewLines()),
                        "expected: \"#NUM!"
                ),
                Arguments.of(
                        Named.of("A4 error not matching \"^#VaL\\\\w[a-z]!$\" CS", "Errors"),
                        cellAt("A4").withErrorText(matching("^#VaL\\w[a-z]!$").caseSensitive()),
                        "to match pattern:"
                ),
                // Booleans
                Arguments.of(
                        Named.of("A1 not false", "Booleans"),
                        cellAt("A1").withBoolean(ofFalse()),
                        "expected: false"
                ),
                Arguments.of(
                        Named.of("A2 not false", "Booleans"),
                        cellAt("A2").withBoolean(equalTo(false)),
                        "expected: false"
                ),
                Arguments.of(
                        Named.of("A3 not true", "Booleans"),
                        cellAt("A3").withBoolean(ofTrue()),
                        "expected: true"
                ),
                // Comments
                Arguments.of(
                        Named.of("A2 does not have comment", "Comments"),
                        cellAt("A2").exists().withComment("no comment"),
                        "comment at Comments!A2 is expected to exist"
                ),
                Arguments.of(
                        Named.of("A1 comment not equal to 'format' CS", "Comments"),
                        cellAt("A1").exists().withComment(equalTo("format").caseSensitive()),
                        "expected: \"format\""
                ),
                Arguments.of(
                        Named.of("A5 comment not containing 'MpT' CS", "Comments"),
                        cellAt("A5").exists().withComment(containing("MpT").caseSensitive()),
                        "to contain:"
                ),
                Arguments.of(
                        Named.of("A9 comment not matching \"\\\\waL[ue]{2}\" CS", "Comments"),
                        cellAt("A9").exists().withComment(matching("\\waL[ue]{2}").caseSensitive()),
                        "to match pattern:"
                )
        );
    }


    @lombok.SneakyThrows
    @BeforeEach
    void setup() {assertThatExcelFile = assertThatExcel(exampleFile);}

    @lombok.SneakyThrows
    @BeforeAll
    static void globalSetup() {
        Assertions.setRemoveAssertJRelatedElementsFromStackTrace(true);

        exampleFile = Files.createTempFile("Example-", ".xlsx").toFile();
        try (FileOutputStream out = new FileOutputStream(exampleFile)) {
            generateTestExcelFile(out);
            //java.awt.Desktop.getDesktop().open(exampleFile);
        }
    }

    @lombok.SneakyThrows
    @AfterAll
    static void deleteTestFile() {
        if (exampleFile != null) Files.deleteIfExists(exampleFile.toPath());
    }

    private static void generateTestExcelFile(OutputStream output) throws IOException {
        try (var workbook = new XSSFWorkbook()) {

            List<SheetEntry> sheetEntries = new ArrayList<>();

            sheetEntries.add(new SheetEntry("Numbers", List.of(
                    FormulaCellEntry.ofNoValue("A1", "1+1", "0.00"),
                    FormulaCellEntry.ofNoValue("A2", "100/3", "0.0000%"),
                    new NumberCellEntry("A3", (double) Float.MAX_VALUE, "0.00"),
                    new NumberCellEntry("A4", (double) Float.MIN_VALUE, "0.00000000"),
                    FormulaCellEntry.ofNoValue("A5", "-9999999", "#,##0"),
                    FormulaCellEntry.ofNoValue("A6", "SQRT(2)", "0.0000"),
                    FormulaCellEntry.ofNoValue("A7", "PI()", "0.0000"),
                    FormulaCellEntry.ofNoValue("A8", "RAND()*100", "0.00")
            )));


            sheetEntries.add(new SheetEntry("Strings", List.of(
                    new TextCellEntry("A1", "Quarterly Report"),
                    FormulaCellEntry.ofNoValue("A2", """
                            "Hello "&"World\""""),
                    FormulaCellEntry.ofNoValue("A3", """
                            TEXT(123456,"##0° 00' 00''")"""),
                    new TextCellEntry("A4", "\"\""),
                    FormulaCellEntry.ofNoValue("A5", """
                            "Line1"&CHAR(10)&"Line2\""""),
                    FormulaCellEntry.ofNoValue("A6", """
                            "123" & "456\""""),
                    FormulaCellEntry.ofNoValue("A7", """
                            "=" & "SUM(1,2)"\s""")
            )));


            sheetEntries.add(new SheetEntry("Dates", List.of(
                    FormulaCellEntry.ofNoValue("A1", "DATE(2023,1,1)", "yyyy-mm-dd"),
                    FormulaCellEntry.ofNoValue("A2", "DATE(1900,1,1)", "yyyy-mm-dd"),
                    FormulaCellEntry.ofNoValue("A3", "TODAY()", "yyyy-mm-dd"),
                    FormulaCellEntry.ofNoValue("A4", "DATE(2024,2,29)", "yyyy-mm-dd"),
                    FormulaCellEntry.ofNoValue("A5", "DATE(1999,12,31)", "yyyy-mm-dd"),
                    FormulaCellEntry.ofNoValue("A6", "EDATE(TODAY(),-1)", "yyyy-mm-dd")
            )));

            sheetEntries.add(new SheetEntry("Times", List.of(
                    FormulaCellEntry.ofNoValue("A1", "TIME(12,0,0)", "hh:mm"),
                    FormulaCellEntry.ofNoValue("A2", "TIME(23,59,59)", "hh:mm:ss"),
                    FormulaCellEntry.ofNoValue("A3", "NOW()-TODAY()", "hh:mm:ss"),
                    FormulaCellEntry.ofNoValue("A4", "TIME(0,0,0)", "hh:mm:ss"),
                    FormulaCellEntry.ofNoValue("A5", "TIME(7,30,15)", "hh:mm:ss AM/PM"),
                    FormulaCellEntry.ofNoValue("A6", "MOD(NOW(),1)", "hh:mm:ss")
            )));

            sheetEntries.add(new SheetEntry("DateTimes", List.of(
                    FormulaCellEntry.ofNoValue("A1", "NOW()", "yyyy-mm-dd hh:mm:ss"),
                    FormulaCellEntry.ofNoValue("A2", "DATE(2025,6,17)+TIME(15,30,0)", "[$-en-US]yyyy-mmm-dd hh:mm:ss;@"),
                    FormulaCellEntry.ofNoValue("A3", "NOW()+1/24", "yyyy-mm-dd hh:mm:ss"),
                    FormulaCellEntry.ofNoValue("A4", "NOW()-1/24", "yyyy-mmm-dd hh:mm:ss"),
                    FormulaCellEntry.ofNoValue("A5", "TODAY()+TIME(23,59,59)", "yyyy-mm-dd hh:mm:ss")
            )));


            sheetEntries.add(new SheetEntry("Booleans", List.of(
                    FormulaCellEntry.ofNoValue("A1", "1=1"),
                    FormulaCellEntry.ofNoValue("A2", "ISNUMBER(123)"),
                    FormulaCellEntry.ofNoValue("A3", "FALSE"),
                    FormulaCellEntry.ofNoValue("A4", "1>2"),
                    FormulaCellEntry.ofNoValue("A5", "NOT(TRUE)"),
                    FormulaCellEntry.ofNoValue("A6", "AND(TRUE,FALSE)"),
                    FormulaCellEntry.ofNoValue("A7", "OR(TRUE,FALSE)")
            )));


            sheetEntries.add(new SheetEntry("Errors", List.of(
                    FormulaCellEntry.ofNoValue("A1", "1/0"),
                    FormulaCellEntry.ofNoValue("A2", "NA()"),
                    FormulaCellEntry.ofNoValue("A3", "SQRT(-1)"),
                    FormulaCellEntry.ofNoValue("A4", """
                            1+"a\""""),
                    FormulaCellEntry.ofNoValue("A5", """
                            INDIRECT("A" & (2^20+1))"""),
                    FormulaCellEntry.ofNoValue("A6", "XYZ()")
            )));


            sheetEntries.add(new SheetEntry("Headers", List.of(
                    new TextCellEntry("E3", "Header"),
                    new TextCellEntry("E4", "First"),
                    new TextCellEntry("E5", "Second"),
                    new TextCellEntry("E6", "Third")
            )));

            sheetEntries.add(new SheetEntry("Comments", List.of(
                    new TextCellEntry("A1", "COMMENT1", null, "FORMAT"),
                    new TextCellEntry("A5", "COMMENT2", null, "EMPTY"),
                    new TextCellEntry("A9", "COMMENT3", null, "VALUE")
            )));


            ExcelOperations.fillWorkbook(workbook, sheetEntries);
            workbook.write(output);
        }
    }
}