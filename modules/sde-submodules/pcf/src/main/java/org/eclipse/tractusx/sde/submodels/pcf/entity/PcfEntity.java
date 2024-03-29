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

package org.eclipse.tractusx.sde.submodels.pcf.entity;

import org.eclipse.tractusx.sde.common.entities.CommonPropEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(name = "pcf_aspect")
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class PcfEntity extends CommonPropEntity {

	@Id
	private String id; // 3893bb5d-da16-4dc1-9185-11d97476c254

	private String uuid;// this for internal identifiaction in database as well

	private String assetLifeCyclePhase;

	private String specVersion; // 2.0.1-20230314
	private String companyId;// urn:uuid:51131FB5-42A2-4267-A402-0ECFEFAD1619
	private String companyName;// My Corp
	private String created;// 2022-05-22T21:47:32Z

	@Column(name = "extWBCSD_pfStatus")
	private String extWBCSDPfStatus;// Active

	@Column(name = "extWBCSD_productCodeCpc")
	private String extWBCSDProductCodeCpc;// 011-99000

	private String productName;// My Product Name
	private Double version; // 0,
	private Double biogenicCarbonEmissionsOtherThanCO2; // 1.0,
	private Double distributionStagePcfExcludingBiogenic; // 1.5,
	private Double biogenicCarbonWithdrawal; // 0.0,
	private Double distributionStageBiogenicCarbonEmissionsOtherThanCO2; // 1.0,

	@Column(name = "extWBCSD_allocationRulesDescription")
	private String extWBCSDAllocationRulesDescription;// In accordance with Catena-X PCF Rulebook

	private String exemptedEmissionsDescription;// No exemption
	private Double distributionStageFossilGhgEmissions; // 0.5,
	private Double exemptedEmissionsPercent; // 0.0,
	private String geographyCountrySubdivision;// US-NY

	@Column(name = "extTFS_luGhgEmissions")
	private Double extTFSLuGhgEmissions; // 0.3,
	private Double distributionStageBiogenicCarbonWithdrawal; // 0.5,
	private Double pcfIncludingBiogenic; // 1.0,
	private Double aircraftGhgEmissions; // 0.0,
	private Double productMassPerDeclaredUnit; // 0.456,

	@Column(name = "extWBCSD_operator")
	private String extWBCSDOperator;// PEF

	private String ruleName;// urn:tfs-initiative.com:PCR:The Product Carbon Footprint Guideline for the
							// Chemical Industry:version:v2.0

	@Column(name = "extWBCSD_otherOperatorName")
	private String extWBCSDOtherOperatorName;// NSF

	@Column(name = "extTFS_allocationWasteIncineration")
	private String extTFSAllocationWasteIncineration;// cut-off
	private Double pcfExcludingBiogenic; // 2.0,
	private String referencePeriodEnd;// 2022-12-31T23:59:59Z

	@Column(name = "extWBCSD_characterizationFactors")
	private String extWBCSDCharacterizationFactors;// AR5
	private String secondaryEmissionFactorSource;// ecoinvent 3.8
	private Double unitaryProductAmount; // 1000.0,
	private String declaredUnit;// liter
	private String referencePeriodStart;// 2022-01-01T00:00:01Z
	private String geographyRegionOrSubregion;// Africa
	private Double fossilGhgEmissions; // 0.5,
	private String boundaryProcessesDescription;// Electricity consumption included as an input in the production phase
	private String geographyCountry;// DE

	@Column(name = "extWBCSD_packagingGhgEmissions")
	private Double extWBCSDPackagingGhgEmissions; // 0,
	private Double dlucGhgEmissions; // 0.4,
	private Double carbonContentTotal; // 2.5,

	@Column(name = "extTFS_distributionStageLuGhgEmissions")
	private Double extTFSDistributionStageLuGhgEmissions; // 1.1,
	private Double primaryDataShare; // 56.12,
	private Double completenessDQR; // 2.0,
	private Double technologicalDQR; // 2.0,
	private Double geographicalDQR; // 2.0,
	private Double temporalDQR; // 2.0,
	private Double reliabilityDQR; // 2.0,
	private Double coveragePercent; // 100,

	@Column(name = "extWBCSD_packagingEmissionsIncluded")
	private String extWBCSDPackagingEmissionsIncluded;// true

	@Column(name = "extWBCSD_fossilCarbonContent")
	private Double extWBCSDFossilCarbonContent; // 0.1,

	private String crossSectoralStandard;// GHG Protocol Product standard

	@Column(name = "extTFS_distributionStageDlucGhgEmissions")
	private Double extTFSDistributionStageDlucGhgEmissions; // 1.0,
	private Double distributionStagePcfIncludingBiogenic; // 0.0,
	private Double carbonContentBiogenic; // 0.0,
	private String partialFullPcf;// Cradle-to-gate
	private String productId;// urn:gtin:4712345060507
	private String validityPeriodStart;// 2022-01-01T00:00:01Z
	private String comment;// Comment for version 42.
	private String validityPeriodEnd;// 2022-12-31T23:59:59Z
	private String pcfLegalStatement;// This PCF (Product Carbon Footprint) is for information purposes only. It is
										// based upon the standards mentioned above.
	private String productDescription;// Ethanol, 95% solution
	private String precedingPfId;// 3893bb5d-da16-4dc1-9185-11d97476c254private String

}