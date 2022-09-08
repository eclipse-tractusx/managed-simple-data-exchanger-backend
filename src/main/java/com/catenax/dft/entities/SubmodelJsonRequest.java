package com.catenax.dft.entities;

import java.util.List;

import com.catenax.dft.validators.UsagePolicyValidation;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmodelJsonRequest<T> {
	
	@JsonProperty(value = "row_data")
	private List<T> rowData;
	
	@JsonProperty(value = "type_of_access")
	private String typeOfAccess;
	
	@JsonProperty(value = "bpn_numbers")
	private List<String> bpnNumbers;

	@JsonProperty(value = "usage_policy")
	@UsagePolicyValidation
	private List<UsagePolicyRequest> usagePolicies;

}
