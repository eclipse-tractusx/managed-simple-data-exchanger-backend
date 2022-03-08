package com.catenax.dft.mapper;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.entities.usecases.Aspect;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface AspectMapper {
    AspectEntity mapFrom(Aspect aspect);
}
