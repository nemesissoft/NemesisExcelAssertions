package io.github.michalbrylka.excelassertion.assertions.text;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class TextAssertionTest {
    @ParameterizedTest(name = "[{index}] \"{0}\" == \"{1}\", IgnoreCase={2}, IgnoreNewLines={3} -> Expected Success: {4}")
    @MethodSource("equalsTextAssertionTestCases")
    void testEqualsTextAssertion(String actualValue, String expected, boolean ignoreCase, boolean ignoreNewLines, boolean expectSuccess) {
        EqualsTextAssertion assertion = new EqualsTextAssertion(expected, ignoreCase, ignoreNewLines);
        AbstractStringAssert<?> stringAssert = assertThat(actualValue);

        if (expectSuccess)
            assertThatNoException().isThrownBy(() -> assertion.apply(stringAssert));
        else
            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertion.apply(stringAssert));
    }

    private static Stream<Arguments> equalsTextAssertionTestCases() {
        return Stream.of(
                // Exact match, case-sensitive
                Arguments.of("Hello World", "Hello World", false, false, true),
                Arguments.of("hello world", "Hello World", false, false, false),
                // Exact match, case-insensitive
                Arguments.of("Hello World", "hello world", true, false, true),
                Arguments.of("hello world", "Hello World", true, false, true),
                // Newline handling, ignoreNewLines = true
                Arguments.of("Hello\nWorld", "Hello World", false, true, true),
                Arguments.of("Hello\r\nWorld", "Hello World", false, true, true),
                Arguments.of("Hello \n World", "Hello World", false, true, true),
                Arguments.of("Hello\nWorld", "hello world", true, true, true), // ignore case and newlines
                Arguments.of("Hello World", "Hello\nWorld", false, true, true), // expected has newlines, actual does not
                Arguments.of("Hello\nWorld", "Hello\n World", false, true, true), // different newline format, but normalize to same
                Arguments.of("Hello\rWorld", "Hello\nWorld", false, true, true), // different newline chars, but normalize to same
                Arguments.of("Line1\nLine2", "Line1Line2", false, true, false), // should fail if normalize removes space
                Arguments.of("Line1\nLine2", "Line1 Line2", false, true, true), // should pass
                Arguments.of("Line1\n Line2", "Line1 Line2", false, true, true), // should pass

                // Newline handling, ignoreNewLines = false
                Arguments.of("Hello\nWorld", "Hello\nWorld", false, false, true),
                Arguments.of("Hello\nWorld", "Hello World", false, false, false),
                Arguments.of("Hello World", "Hello\nWorld", false, false, false),

                // Null values
                Arguments.of(null, null, false, false, true),
                Arguments.of("Hello", null, false, false, false),
                Arguments.of(null, "Hello", false, false, false),

                Arguments.of(null, null, true, false, true),
                Arguments.of("Hello", null, true, false, false),
                Arguments.of(null, "Hello", true, false, false),

                Arguments.of(null, null, false, true, true),
                Arguments.of("Hello", null, false, true, false),
                Arguments.of(null, "Hello", false, true, false),

                Arguments.of(null, null, true, true, true),
                Arguments.of("Hello", null, true, true, false),
                Arguments.of(null, "Hello", true, true, false)
        );
    }

    @ParameterizedTest(name = "[{index}] \"{1}\" in \"{0}\" IgnoreCase={2} -> Expected Success: {3}")
    @MethodSource("containsTextAssertionTestCases")
    void testContainsTextAssertion(String actualValue, String expectedSubstring, boolean ignoreCase, boolean expectSuccess) {
        ContainsTextAssertion assertion = new ContainsTextAssertion(expectedSubstring, ignoreCase);
        AbstractStringAssert<?> stringAssert = assertThat(actualValue);

        if (expectSuccess)
            assertThatNoException().isThrownBy(() -> assertion.apply(stringAssert));
        else
            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertion.apply(stringAssert));
    }

    private static Stream<Arguments> containsTextAssertionTestCases() {
        return Stream.of(
                // Case-sensitive contains
                Arguments.of("Hello World", "World", false, true),
                Arguments.of("Hello World", "world", false, false),
                Arguments.of("Another test", "test", false, true),
                // Case-insensitive contains
                Arguments.of("Hello World", "world", true, true),
                Arguments.of("Another Test", "test", true, true),
                // Edge cases
                Arguments.of("Hello", "Hello", false, true), // exact match
                Arguments.of("", "", false, true), // empty string contains empty string
                Arguments.of("Hello", "", false, true), // any string contains empty string
                Arguments.of("", "abc", false, false), // empty string does not contain non-empty
                // Null values
                Arguments.of(null, "test", false, false) // null cannot contain anything
        );
    }

    // Test for PatternTextAssertion
    @ParameterizedTest
    @MethodSource("patternTextAssertionTestCases")
    void testPatternTextAssertion(String actualValue, String regex, boolean ignoreCase, boolean singleLineMode, boolean expectSuccess) {
        PatternTextAssertion assertion = new PatternTextAssertion(regex, ignoreCase, singleLineMode);
        AbstractStringAssert<?> stringAssert = assertThat(actualValue);

        if (expectSuccess)
            assertThatNoException().isThrownBy(() -> assertion.apply(stringAssert));
        else
            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertion.apply(stringAssert));
    }

    private static Stream<Arguments> patternTextAssertionTestCases() {
        return Stream.of(
                // Basic regex match
                Arguments.of("Hello World", "Hello.*", false, false, true),
                Arguments.of("Hello World", "hello.*", false, false, false),
                // Case-insensitive
                Arguments.of("Hello World", "hello.*", true, false, true),
                Arguments.of("another test", "ANOTHER.*", true, false, true),
                // Single-line mode (DOTALL)
                Arguments.of("Line1\nLine2", "Line1.Line2", false, false, false), // . doesn't match newline without DOTALL
                Arguments.of("Line1\nLine2", "Line1.Line2", false, true, true),  // . matches newline with DOTALL
                Arguments.of("Line1\nLine2", "line1.line2", true, true, true),   // case-insensitive + DOTALL
                // Specific regex patterns
                Arguments.of("123-456-7890", "\\d{3}-\\d{3}-\\d{4}", false, false, true),
                Arguments.of("abc", "[a-c]+", false, false, true),
                Arguments.of("def", "[a-c]+", false, false, false),
                // Null values
                Arguments.of(null, ".*", false, false, false) // null doesn't match any pattern
        );
    }

    static final ObjectMapper objectMapper = new ObjectMapper();

    static Stream<Arguments> serializationCases() {
        return Stream.of(
                Arguments.of(new EqualsTextAssertion("test", false, false), """
                        {"eq":"test"}"""),
                Arguments.of(new EqualsTextAssertion("A=B", true, true), """
                        {"eq":"A=B","ignoreNewLines":true,"ignoreCase":true}"""),
                Arguments.of(new ContainsTextAssertion("hello", false), """
                        {"has":"hello"}"""),
                Arguments.of(new ContainsTextAssertion("world", true), """
                        {"has":"world","ignoreCase":true}"""),
                Arguments.of(new PatternTextAssertion("a.*b", false, false), """
                        {"like":"a.*b"}"""),
                Arguments.of(new PatternTextAssertion("abc", true, true), """
                        {"like":"abc","dotall":true,"ignoreCase":true}""")
        );
    }

    @ParameterizedTest(name = "should serialize {0} into {1}")
    @MethodSource("serializationCases")
    void shouldSerializeTextAssertions(TextAssertion<?> assertion, String expectedJson) throws JsonProcessingException {
        String actualJson = objectMapper.writeValueAsString(assertion);
        assertThat(actualJson).isEqualTo(expectedJson);
    }

    static Stream<Arguments> deserializationCases() {
        return Stream.of(
                Arguments.of("""
                                {"eq":"test"}""",
                        new EqualsTextAssertion("test", false, false)),
                Arguments.of("""
                                {"==":"abc","ignoreCase":true,"ignoreNewLines":true}""",
                        new EqualsTextAssertion("abc", true, true)),
                Arguments.of("""
                                {"has":"hello"}""",
                        new ContainsTextAssertion("hello", false)),
                Arguments.of("""
                                {"∋":"world","ignoreCase":true}""",
                        new ContainsTextAssertion("world", true)),
                Arguments.of("""
                                {"like":"a.*b"}""",
                        new PatternTextAssertion("a.*b", false, false)),
                Arguments.of("""
                                {"like":"abc","ignoreCase":true,"dotall":true}""",
                        new PatternTextAssertion("abc", true, true))
        );
    }

    @ParameterizedTest(name = "should deserialize {1} from {0}")
    @MethodSource("deserializationCases")
    void shouldDeserializeTextAssertions(String json, TextAssertion<?> expected) throws JsonProcessingException {
        TextAssertion<?> result = objectMapper.readValue(json, TextAssertion.class);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    static Stream<Arguments> invalidJsonCases() {
        return Stream.of(
                Arguments.of(Named.of("no operation key", """
                        {"ignoreCase":true}"""), "Exactly one operation key must be present"),

                Arguments.of(Named.of("multiple operations", """
                        {"eq":"test", "==":"test"}"""), "Exactly one operation key must be present"),

                Arguments.of(Named.of("unknown field present", """
                        {"eq":"test", "extra":true}"""), "Unknown field: extra"),

                Arguments.of(Named.of("null expected value", """
                        {"eq":null}"""), "The value for 'eq' must not be null"),

                Arguments.of(Named.of("unknown operation alias", """
                        {"equals":"abc"}"""), "Unknown field: equals")
        );
    }

    @ParameterizedTest(name = "should reject invalid JSON: {0}")
    @MethodSource("invalidJsonCases")
    void shouldRejectInvalidJson(String namedJson, String expectedMessageFragment) {
        assertThatThrownBy(() -> objectMapper.readValue(namedJson, TextAssertion.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining(expectedMessageFragment);
    }

}