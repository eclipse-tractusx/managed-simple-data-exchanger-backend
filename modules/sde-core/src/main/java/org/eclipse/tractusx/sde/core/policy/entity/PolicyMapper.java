package org.eclipse.tractusx.sde.core.policy.entity;

import org.eclipse.tractusx.sde.common.entities.SubmodelPolicyRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

	SubmodelPolicyRequest mapFrom(PolicyEntity entiry);

	PolicyEntity mapFrom(SubmodelPolicyRequest pojo);
}
