package com.mkorpar.productservice.mappers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CoreModelMapperTest {

    private final CoreModelMapper mapper = new CoreModelMapper(List.of());

    @Test
    void shouldCorrectlyMapList_WhenSourceListIsNotEmpty() {
        // Arrange
        List<Source> sources = List.of(new Source("apple"), new Source("banana"));

        // Act
        List<Target> targetList = mapper.mapList(sources, Target.class);

        // Assert
        assertThat(targetList).hasSize(2);
        assertThat(targetList).first()
                .extracting(Target::getData)
                .isEqualTo("apple");
        assertThat(targetList).last()
                .extracting(Target::getData)
                .isEqualTo("banana");
    }

    @Test
    void shouldReturnEmptyList_WhenSourceListIsEmpty() {
        List<Target> targetList = mapper.mapList(List.of(), Target.class);
        assertThat(targetList).isEmpty();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Source {
        private String data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Target {
        private String data;
    }

}
