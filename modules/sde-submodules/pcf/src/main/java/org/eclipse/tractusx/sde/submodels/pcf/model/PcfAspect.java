/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.submodels.pcf.model;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcfAspect {

	@JsonProperty(value = "shell_id")
	private String shellIdforPcf;

	@JsonProperty(value = "sub_model_id")
	private String subModelIdforPcf;

	@JsonProperty(value = "row_number")
	private int rowNumberforPcf;

	@JsonProperty(value = "bpn_numbers")
	private List<String> bpnNumbersforPcf;

	@JsonProperty(value = "type_of_access")
	private String typeOfAccessforPcf;

	@JsonProperty(value = "usage_policies")
	private List<UsagePolicies> usagePoliciesforPcf;

	@JsonProperty(value = "process_id")
	private String processIdforPcf;
	
	private String id;
	
	private String uuid;
	
	@JsonProperty(value = "manufacturer_part_id")
	private String manufacturerPartId;
	
	@JsonProperty(value = "name_at_manufacturer")
	private String nameAtManufacturer;
	
	private String assetLifeCyclePhase;

	private String specVersion;
	private String companyId;
	private String companyName;
	private String created;
	
	@JsonProperty(value = "extWBCSD_pfStatus")
	private String extWBCSDPfStatus;
	
	@JsonProperty(value = "extWBCSD_productCodeCpc")
	private String extWBCSDProductCodeCpc;
	private String productName;
	private String version;
	private String biogenicCarbonEmissionsOtherThanCO2;
	private String distributionStagePcfExcludingBiogenic;
	private String biogenicCarbonWithdrawal;
	private String distributionStageBiogenicCarbonEmissionsOtherThanCO2;
	
	@JsonProperty(value = "extWBCSD_allocationRulesDescription")
	private String extWBCSDAllocationRulesDescription;
	private String exemptedEmissionsDescription;
	private String distributionStageFossilGhgEmissions;
	private String exemptedEmissionsPercent;
	private String geographyCountrySubdivision;

	@JsonProperty(value = "extTFS_luGhgEmissions")
	private String extTFSLuGhgEmissions;
	private String distributionStageBiogenicCarbonWithdrawal;
	private String pcfIncludingBiogenic;
	private String aircraftGhgEmissions;
	private String productMassPerDeclaredUnit;
	
	@JsonProperty(value = "extWBCSD_operator")
	private String extWBCSDOperator;
	
	private String ruleName;
	
	@JsonProperty(value = "extWBCSD_otherOperatorName")
	private String extWBCSDOtherOperatorName;
	
	@JsonProperty(value = "extTFS_allocationWasteIncineration")
	private String extTFSAllocationWasteIncineration;
	private String pcfExcludingBiogenic;
	private String referencePeriodEnd;
	
	@JsonProperty(value = "extWBCSD_characterizationFactors")
	private String extWBCSDCharacterizationFactors;
	private String secondaryEmissionFactorSource;
	private String unitaryProductAmount;
	private String declaredUnit;
	private String referencePeriodStart;
	private String geographyRegionOrSubregion;
	private String fossilGhgEmissions;
	private String boundaryProcessesDescription;
	private String geographyCountry;
	
	@JsonProperty(value = "extWBCSD_packagingGhgEmissions")
	private String extWBCSDPackagingGhgEmissions;
	private String dlucGhgEmissions;
	private String carbonContentTotal;
	
	@JsonProperty(value = "extTFS_distributionStageLuGhgEmissions")
	private String extTFSDistributionStageLuGhgEmissions;
	private String primaryDataShare;
	private String completenessDQR;
	private String technologicalDQR;
	private String geographicalDQR;
	private String temporalDQR;
	private String reliabilityDQR;
	private String coveragePercent;
	
	@JsonProperty(value = "extWBCSD_packagingEmissionsIncluded")
	private boolean extWBCSDPackagingEmissionsIncluded;
	
	@JsonProperty(value = "extWBCSD_fossilCarbonContent")
	private String extWBCSDFossilCarbonContent;
	private String crossSectoralStandard;
	
	@JsonProperty(value = "extTFS_distributionStageDlucGhgEmissions")
	private String extTFSDistributionStageDlucGhgEmissions;
	private String distributionStagePcfIncludingBiogenic;
	private String carbonContentBiogenic;
	private String partialFullPcf;
	private String productId;
	private String validityPeriodStart;
	private String comment;
	private String validityPeriodEnd;
	private String pcfLegalStatement;
	private String productDescription;
	private String precedingPfId;

	@JsonProperty(value = "contract_defination_id")
	private String contractDefinationIdforPcf;

	@JsonProperty(value = "usage_policy_id")
	private String usagePolicyIdforPcf;

	@JsonProperty(value = "access_policy_id")
	private String accessPolicyIdforPcf;

	@JsonProperty(value = "asset_id")
	private String assetIdforPcf;

	@JsonProperty(value = "updated")
	private String updatedforPcf;

}
