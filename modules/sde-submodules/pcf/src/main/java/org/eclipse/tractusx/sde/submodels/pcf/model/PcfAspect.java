/********************************************************************************
 * Copyright (c) 2023, 2024 T-Systems International GmbH
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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


import org.eclipse.tractusx.sde.common.entities.CommonPropEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class PcfAspect extends CommonPropEntity {

	@Id
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

}
