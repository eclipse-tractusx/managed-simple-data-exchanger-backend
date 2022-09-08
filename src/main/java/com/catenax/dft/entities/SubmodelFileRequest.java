package com.catenax.dft.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmodelFileRequest {

	@JsonProperty(value = "type_of_access")
	private String typeOfAccess;
	
	@JsonProperty(value = "bpn_numbers")
	private List<String> bpnNumbers;

	@JsonProperty(value = "usage_policy")
	private List<UsagePolicyRequest> usagePolicies;
}
