package com.catenax.dft.mapper;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.entities.usecases.Aspect;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface AspectEntityMapper {


    Aspect mapFrom(AspectEntity aspectEntity);

    AspectEntity mapFrom(Aspect aspect);

}
