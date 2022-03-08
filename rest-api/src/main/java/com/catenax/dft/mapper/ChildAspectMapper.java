package com.catenax.dft.mapper;

import com.catenax.dft.entities.database.ChildAspectEntity;
import com.catenax.dft.entities.usecases.ChildAspect;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")

public interface ChildAspectMapper {

    ChildAspectEntity mapFrom(ChildAspect childAspect);
}
