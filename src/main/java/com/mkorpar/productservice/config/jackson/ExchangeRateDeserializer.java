package com.mkorpar.productservice.config.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class ExchangeRateDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

        try {
            Number number = format.parse(parser.getText());
            return new BigDecimal(number.toString());
        } catch (ParseException e) {
            String exceptionMsg = "Failed to parse BigDecimal with ',' as decimal separator and '.' as group separator";
            throw new JsonParseException(parser, exceptionMsg, e);
        }
    }

}
