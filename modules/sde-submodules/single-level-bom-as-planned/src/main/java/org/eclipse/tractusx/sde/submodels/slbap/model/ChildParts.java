package org.eclipse.tractusx.sde.submodels.slbap.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChildParts {
	
	private Quantity quantity;
    private String createdOn;
    private String lastModifiedOn;
    private String childCatenaXId;

}
