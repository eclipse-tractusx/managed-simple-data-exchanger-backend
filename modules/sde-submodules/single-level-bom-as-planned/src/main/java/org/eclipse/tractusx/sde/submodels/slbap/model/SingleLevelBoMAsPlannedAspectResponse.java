package org.eclipse.tractusx.sde.submodels.slbap.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class SingleLevelBoMAsPlannedAspectResponse {
	
    private String catenaXId;
    private Set<ChildParts> childParts;

}
