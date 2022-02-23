package com.catenax.dft.mapper;

import com.catenax.dft.dao.AspectDao;
import com.catenax.dft.entities.Aspect;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface AspectEntityDaoMapper {

    Aspect mapFrom(AspectDao aspectDao);

    AspectDao mapFrom(Aspect aspect);

}
