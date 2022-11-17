package org.eclipse.tractusx.sde.submodels.slbap.model;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.UsagePolicy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SingleLevelBoMAsPlanned {

	@JsonProperty(value = "shell_id")
	private String shellId;

	private String subModelId;

	@JsonProperty(value = "row_number")
	private int rowNumber;

	@JsonProperty(value = "uuid")
	private String uuid;

	@JsonProperty(value = "process_id")
	private String processId;

	@JsonProperty(value = "contract_defination_id")
	private String contractDefinationId;

	@JsonProperty(value = "usage_policy_id")
	private String usagePolicyId;

	@JsonProperty(value = "asset_id")
	private String assetId;

	@JsonProperty(value = "access_policy_id")
	private String accessPolicyId;

	@JsonProperty(value = "deleted")
	private String deleted;

	@JsonProperty(value = "bpn_numbers")
	private List<String> bpnNumbers;

	@JsonProperty(value = "type_of_access")
	private String typeOfAccess;

	@JsonProperty(value = "usage_policy")
	private List<UsagePolicy> usagePolicies;

	@JsonProperty(value = "manufacturer_part_id")
	private String manufacturerPartId;

	@JsonProperty(value = "quantity_number")
	private String quantityNumber;
	
	@JsonProperty(value = "measurement_unit_lexical_value")
	private String lexicalValue;
	
	@JsonProperty(value = "datatype_url")
	private String datatypeUrl;
	
	@JsonProperty(value = "created_on")
	private String createdOn;

	@JsonProperty(value = "last_modified_on")
	private String lastModifiedOn;
	
	@JsonProperty(value = "child_uuid")
	private String childUuid;
	
}
