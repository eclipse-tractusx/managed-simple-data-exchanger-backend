package org.eclipse.tractusx.sde.submodels.pap.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ValidityPeriod {
	
    private String validFrom;
    private String validTo;

}
