package org.nemesis.excelassertion.assertions.text;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.assertj.core.api.AbstractStringAssert;

import java.io.IOException;
import java.util.*;

@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = TextAssertion.TextAssertionSerializer.class)
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = TextAssertion.TextAssertionDeserializer.class)
public sealed abstract class TextAssertion<TAssertion extends TextAssertion<TAssertion>> permits ContainsTextAssertion, EqualsTextAssertion, PatternTextAssertion {
    boolean ignoreCase;

    protected TextAssertion(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public TAssertion ignoreCase() {
        this.ignoreCase = true;
        return self();
    }

    public TAssertion caseSensitive() {
        this.ignoreCase = false;
        return self();
    }

    @Override
    public abstract String toString();

    @SuppressWarnings("unchecked")
    protected TAssertion self() {
        return (TAssertion) this;
    }

    public abstract void apply(AbstractStringAssert<? extends AbstractStringAssert<?>> assertion);

    private static final String IGNORE_CASE = "ignoreCase";
    private static final String IGNORE_NEW_LINES = "ignoreNewLines";
    private static final String DOTALL = "dotall";

    private static final Set<String> ALL_OPERATION_KEYS = Set.of("eq", "=", "==", "has", "∋", "like");
    private static final Set<String> ALL_OPTION_KEYS = Set.of(IGNORE_CASE, IGNORE_NEW_LINES, DOTALL);
    private static final Set<String> ALL_ALLOWED_KEYS;

    static {
        Set<String> combined = new HashSet<>(ALL_OPERATION_KEYS);
        combined.addAll(ALL_OPTION_KEYS);
        ALL_ALLOWED_KEYS = Collections.unmodifiableSet(combined);
    }

    static class TextAssertionSerializer extends JsonSerializer<TextAssertion<?>> {
        @Override
        public void serialize(TextAssertion<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeStartObject();

            switch (value) {
                case EqualsTextAssertion eta -> {
                    gen.writeStringField("eq", eta.expected);
                    if (eta.ignoreNewLines) gen.writeBooleanField(IGNORE_NEW_LINES, true);
                }

                case ContainsTextAssertion cta -> gen.writeStringField("has", cta.expectedSubstring);

                case PatternTextAssertion pta -> {
                    gen.writeStringField("like", pta.pattern);
                    if (pta.dotallMode) gen.writeBooleanField(DOTALL, true);
                }
                default -> throw new JsonMappingException(gen, "Unknown TextAssertion subtype: " + value.getClass());
            }

            if (value.ignoreCase) gen.writeBooleanField(IGNORE_CASE, true);

            gen.writeEndObject();
        }
    }

    static class TextAssertionDeserializer extends JsonDeserializer<TextAssertion<?>> {

        @Override
        public TextAssertion<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);

            // Validate all field names
            Iterator<String> fieldNames = node.fieldNames();
            Set<String> presentFields = new HashSet<>();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                if (!ALL_ALLOWED_KEYS.contains(field))
                    throw new JsonMappingException(p, "Unknown field: " + field);
                presentFields.add(field);
            }

            // Find exactly one operation
            List<String> presentOps = ALL_OPERATION_KEYS.stream()
                    .filter(presentFields::contains)
                    .toList();

            if (presentOps.size() != 1)
                throw new JsonMappingException(p, "Exactly one operation key must be present, found: " + presentOps);

            // Read boolean options with default `false`
            boolean ignoreCase = node.path(IGNORE_CASE).asBoolean(false);
            boolean ignoreNewLines = node.path(IGNORE_NEW_LINES).asBoolean(false);
            boolean dotallMode = node.path(DOTALL).asBoolean(false);


            // Deserialize based on operation
            String op = presentOps.getFirst();
            if (!(node.get(op) instanceof JsonNode argumentNode) || argumentNode.isNull())
                throw new JsonMappingException(p, "The value for '%s' must not be null".formatted(op));

            return switch (op) {
                case "eq", "=", "==" -> {
                    String expected = argumentNode.asText();
                    yield new EqualsTextAssertion(expected, ignoreCase, ignoreNewLines);
                }
                case "has", "∋" -> {
                    String substring = argumentNode.asText();
                    yield new ContainsTextAssertion(substring, ignoreCase);
                }
                case "like" -> {
                    String pattern = argumentNode.asText();
                    yield new PatternTextAssertion(pattern, ignoreCase, dotallMode);
                }
                default -> throw new JsonMappingException(p, "Unhandled operation key: " + op);
            };
        }
    }
}