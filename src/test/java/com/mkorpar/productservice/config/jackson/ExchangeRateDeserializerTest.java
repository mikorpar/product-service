package com.mkorpar.productservice.config.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

class ExchangeRateDeserializerTest {

    private final ExchangeRateDeserializer deserializer = new ExchangeRateDeserializer();
    private final JsonFactory jsonFactory = new JsonFactory();

    @ParameterizedTest
    @ValueSource(strings = { "1.234,56", "123,45", "1.000" })
    void shouldSuccessfullyDeserializeNumber(String numberAsString) {
        // Arrange
        String expectedVal = numberAsString.replaceAll("\\.", "").replace(',', '.');

        // Act && Assert
        runTest(numberAsString, false, actualVal ->
            assertThat(actualVal).isEqualTo(new BigDecimal(expectedVal))
        );
    }

    @Test
    void shouldThrownExceptionWhenValueIsInvalid() {
        // Arrange
        String numberAsString = "numberAsString";

        // Act && Assert
        runTest(numberAsString, true, actualVal -> {});
    }

    private void runTest(String numberAsString, boolean parseExceptionExpected, Consumer<BigDecimal> assertConsumer) {
        try (JsonParser parser = createParser(numberAsString)) {
            // Act
            BigDecimal deserializedVal = deserializer.deserialize(parser, null);

            // Assert
            assertConsumer.accept(deserializedVal);
        } catch (JsonParseException e) {
            if (!parseExceptionExpected) {
                // Assert
                fail(e);
            }
        } catch (IOException e) {
            // Assert
            fail(e);
        }
    }

    private JsonParser createParser(String value) throws IOException {
        JsonParser parser = jsonFactory.createParser("\"" + value + "\"");
        parser.nextToken();
        return parser;
    }

}