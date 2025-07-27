package com.mkorpar.productservice.utils;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.Serial;
import java.lang.reflect.Field;

public final class PropertyNamingStrategyUtils {

    private PropertyNamingStrategyUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * @param qualifier the fully qualified {@link PropertyNamingStrategies.NamingBase} child class name or the field name
     *                 corresponding to {@link PropertyNamingStrategy} constant in {@link PropertyNamingStrategies} class
     */
    public static PropertyNamingStrategies.NamingBase getStrategy(String qualifier) {
        if (qualifier == null) {
            return NullPropertyNamingStrategy.getInstance();
        }

        try {
            return getStrategyByClassName(qualifier);
        } catch (ClassNotFoundException ignored) {
            return getStrategyByPropertyNamingStrategiesFieldName(qualifier);
        }
    }

    private static PropertyNamingStrategies.NamingBase getStrategyByClassName(String name) throws ClassNotFoundException {
        Class<?> clazz = ClassUtils.forName(name, null);

        Object instance = BeanUtils.instantiateClass(clazz);
        if (instance instanceof PropertyNamingStrategies.NamingBase strategy) {
            return strategy;
        }
        return NullPropertyNamingStrategy.getInstance();
    }

    private static PropertyNamingStrategies.NamingBase getStrategyByPropertyNamingStrategiesFieldName(String name) {
        Field field = ReflectionUtils.findField(PropertyNamingStrategies.class, name, PropertyNamingStrategy.class);

        if (field == null) {
            return NullPropertyNamingStrategy.getInstance();
        }

        try {
            if (field.get(null) instanceof PropertyNamingStrategies.NamingBase strategy) {
                return strategy;
            }
            return NullPropertyNamingStrategy.getInstance();
        } catch (IllegalAccessException ignored) {
            return NullPropertyNamingStrategy.getInstance();
        }
    }

    private static class NullPropertyNamingStrategy extends PropertyNamingStrategies.NamingBase {
        @Serial
        private static final long serialVersionUID = 1L;

        public final static NullPropertyNamingStrategy INSTANCE = new NullPropertyNamingStrategy();

        private NullPropertyNamingStrategy() {}

        public static PropertyNamingStrategies.NamingBase getInstance() {
            return INSTANCE;
        }

        @Override
        public String translate(String input) {
            return input;
        }
    }

}
