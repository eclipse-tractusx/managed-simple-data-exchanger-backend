package org.eclipse.tractusx.sde.submodels.pap.model;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.UsagePolicy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PartAsPlanned {

	private String shellId;
	private String subModelId;
	
	private int rowNumber;
	private String uuid;
	private String processId;
	
	private String contractDefinationId;
	private String usagePolicyId;
	private String assetId;
	private String accessPolicyId;
	private String deleted;

	private List<String> bpnNumbers;
	private String typeOfAccess;
	private List<UsagePolicy> usagePolicies;

	private String manufacturerPartId;
	private String classification;
	private String nameAtManufacturer;
    private String validFrom;
    private String validTo;
}
