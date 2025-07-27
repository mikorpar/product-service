package com.mkorpar.productservice.mappers;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoreModelMapper extends ModelMapper {

    private final List<PropertyMap<?, ?>> propertyMaps;

    @PostConstruct
    private void addAdditionalMappings() {
        propertyMaps.forEach(this::addMappings);
    }

    public <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream()
                .map(elem -> this.map(elem, targetClass))
                .toList();
    }

}
