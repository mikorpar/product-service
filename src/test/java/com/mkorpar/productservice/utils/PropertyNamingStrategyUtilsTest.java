package com.mkorpar.productservice.utils;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.*;

class PropertyNamingStrategyUtilsTest {

    @Test
    void shouldReturnSnakeCaseStrategy_WhenGivenSupportedClassName() {
        // Arrange
        String className = PropertyNamingStrategies.SnakeCaseStrategy.class.getName();

        // Act
        shouldReturnSnakeCaseStrategy(className);
    }

    @Test
    void shouldReturnSnakeCaseStrategy_WhenGivenSupportedFieldName() {
        // Arrange
        String fieldName = "SNAKE_CASE";

        // Act
        shouldReturnSnakeCaseStrategy(fieldName);
    }

    @Test
    void shouldReturnNullStrategy_WhenGivenNull() {
        // Arrange
        shouldReturnNullStrategy(null);
    }

    @Test
    void shouldReturnNullStrategy_WhenGivenUnsupportedClassName() {
        // Arrange
        String className = "java.util.Date";

        // Act
        shouldReturnNullStrategy(className);
    }

    @Test
    void shouldReturnNullStrategy_WhenGivenUnknownClassName() {
        // Arrange
        String className = "com.unknown.ClassName";

        // Act
        shouldReturnNullStrategy(className);
    }

    @Test
    void shouldReturnNullStrategy_WhenGivenUnknownFieldName() {
        // Arrange
        String fieldName = "UNKNOWN_CASE";

        // Act
        shouldReturnNullStrategy(fieldName);
    }

    @Test
    void shouldThrowException_WhenTryingToInstantiateClass() throws Exception {
        Constructor<PropertyNamingStrategyUtils> constructor =
                PropertyNamingStrategyUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .hasRootCauseMessage("Utility class cannot be instantiated");
    }

    private void shouldReturnSnakeCaseStrategy(String qualifier) {
        // Act
        PropertyNamingStrategies.NamingBase strategy =
                PropertyNamingStrategyUtils.getStrategy(qualifier);

        // Assert
        assertThat(strategy).isInstanceOf(PropertyNamingStrategies.SnakeCaseStrategy.class);
    }

    private void shouldReturnNullStrategy(String qualifier) {
        // Act
        PropertyNamingStrategies.NamingBase strategy =
                PropertyNamingStrategyUtils.getStrategy(qualifier);

        // Assert
        assertThat(strategy).isInstanceOf(PropertyNamingStrategyUtils.NullPropertyNamingStrategy.class);
    }

}
