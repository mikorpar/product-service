package com.mkorpar.productservice.config.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ExchangeRateDeserializerTest {

    private final ExchangeRateDeserializer deserializer = new ExchangeRateDeserializer();
    private final JsonFactory jsonFactory = new JsonFactory();

    @Test
    void shouldDeserializeNumberContainingGroupAndDecimalSeparator() throws IOException {
        // Arrange
        JsonParser parser = createParser("1.234,56");

        // Act
        BigDecimal result = deserializer.deserialize(parser, null);

        // Assert
        assertThat(result).isEqualTo(new BigDecimal("1234.56"));
    }

    @Test
    void shouldDeserializeNumberContainingOnlyDecimalSeparator() throws IOException {
        // Arrange
        JsonParser parser = createParser("123,45");

        // Act
        BigDecimal result = deserializer.deserialize(parser, null);

        // Assert
        assertThat(result).isEqualTo(new BigDecimal("123.45"));
    }

    @Test
    void shouldDeserializeNumberContainingOnlyGroupSeparator() throws IOException {
        // Arrange
        JsonParser parser = createParser("1.000");

        // Act
        BigDecimal result = deserializer.deserialize(parser, null);

        // Assert
        assertThat(result).isEqualTo(new BigDecimal("1000"));
    }

    @Test
    void shouldThrownExceptionWhenValueIsInvalid() throws IOException {
        // Arrange
        JsonParser parser = createParser("not_a_number");

        // Act && Assert
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() ->
            deserializer.deserialize(parser, null)
        );
    }

    private JsonParser createParser(String value) throws IOException {
        JsonParser parser = jsonFactory.createParser("\"" + value + "\"");
        parser.nextToken();
        return parser;
    }

}