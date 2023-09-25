package org.eclipse.tractusx.sde.agent.mapper;

import org.eclipse.tractusx.sde.agent.entity.ConfigEntity;
import org.eclipse.tractusx.sde.agent.model.ConfigResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AutoUploadAgentConfigMapper {

	ConfigResponse mapFrom(ConfigEntity entity);

	ConfigEntity mapFrom(ConfigResponse pojo);

}
