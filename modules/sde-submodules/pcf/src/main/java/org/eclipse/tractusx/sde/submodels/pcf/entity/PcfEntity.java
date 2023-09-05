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

package org.eclipse.tractusx.sde.submodels.pcf.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "pcf_aspect")
@Entity
@Data
public class PcfEntity implements Serializable {

	@Id
    private String id; //3893bb5d-da16-4dc1-9185-11d97476c254
    
    private String uuid;//this for internal identifiaction in database as well

    @Column(name = "process_id")
    private String processIdforPcf;
    
	private String assetLifeCyclePhase;
    
	private String specVersion; //2.0.1-20230314
	private String companyId;//urn:uuid:51131FB5-42A2-4267-A402-0ECFEFAD1619
	private String companyName;//My Corp
	private String created;//2022-05-22T21:47:32Z
	
	@Column(name = "extWBCSD_pfStatus")
	private String extWBCSDPfStatus;//Active
	
	@Column(name = "extWBCSD_productCodeCpc")
	private String extWBCSDProductCodeCpc;//011-99000
	
	private String productName;//My Product Name
	private double version; //0,
	private double biogenicCarbonEmissionsOtherThanCO2; // 1.0,
	private double distributionStagePcfExcludingBiogenic; // 1.5,
	private double biogenicCarbonWithdrawal; // 0.0,
	private double distributionStageBiogenicCarbonEmissionsOtherThanCO2; // 1.0,
	
	@Column(name = "extWBCSD_allocationRulesDescription")
	private String extWBCSDAllocationRulesDescription;//In accordance with Catena-X PCF Rulebook
	
	private String exemptedEmissionsDescription;//No exemption
	private double distributionStageFossilGhgEmissions; // 0.5,
	private double exemptedEmissionsPercent; // 0.0,
	private String geographyCountrySubdivision;//US-NY
	
	@Column(name = "extTFS_luGhgEmissions")
	private double extTFSLuGhgEmissions; // 0.3,
	private double distributionStageBiogenicCarbonWithdrawal; // 0.5,
	private double pcfIncludingBiogenic; // 1.0,
	private double aircraftGhgEmissions; // 0.0,
	private double productMassPerDeclaredUnit; // 0.456,
	
	@Column(name = "extWBCSD_operator")
	private String extWBCSDOperator;//PEF
	
	private String ruleName;//urn:tfs-initiative.com:PCR:The Product Carbon Footprint Guideline for the Chemical Industry:version:v2.0
	
	@Column(name = "extWBCSD_otherOperatorName")
	private String extWBCSDOtherOperatorName;//NSF
	
	@Column(name = "extTFS_allocationWasteIncineration")
	private String extTFSAllocationWasteIncineration;//cut-off
	private double pcfExcludingBiogenic; // 2.0,
	private String referencePeriodEnd;//2022-12-31T23:59:59Z
	
	@Column(name = "extWBCSD_characterizationFactors")
	private String extWBCSDCharacterizationFactors;//AR5
	private String secondaryEmissionFactorSource;//ecoinvent 3.8
	private double unitaryProductAmount; // 1000.0,
	private String declaredUnit;//liter
	private String referencePeriodStart;//2022-01-01T00:00:01Z
	private String geographyRegionOrSubregion;//Africa
	private double fossilGhgEmissions; // 0.5,
	private String boundaryProcessesDescription;//Electricity consumption included as an input in the production phase
	private String geographyCountry;//DE
	
	@Column(name = "extWBCSD_packagingGhgEmissions")
	private double extWBCSDPackagingGhgEmissions; // 0,
	private double dlucGhgEmissions; // 0.4,
	private double carbonContentTotal; // 2.5,
	
	@Column(name = "extTFS_distributionStageLuGhgEmissions")
	private double extTFSDistributionStageLuGhgEmissions; // 1.1,
	private double primaryDataShare; // 56.12,
	private double completenessDQR; // 2.0,
	private double technologicalDQR; // 2.0,
	private double geographicalDQR; // 2.0,
	private double temporalDQR; // 2.0,
	private double reliabilityDQR; // 2.0,
	private double coveragePercent; // 100,
	
	@Column(name = "extWBCSD_packagingEmissionsIncluded")
	private String extWBCSDPackagingEmissionsIncluded;//true
	
	@Column(name = "extWBCSD_fossilCarbonContent")
	private double extWBCSDFossilCarbonContent; // 0.1,
	
	private String crossSectoralStandard;//GHG Protocol Product standard
	
	@Column(name = "extTFS_distributionStageDlucGhgEmissions")
	private double extTFSDistributionStageDlucGhgEmissions; // 1.0,
	private double distributionStagePcfIncludingBiogenic; // 0.0,
	private double carbonContentBiogenic; // 0.0,
	private String partialFullPcf;//Cradle-to-gate
	private String productId;//urn:gtin:4712345060507
	private String validityPeriodStart;//2022-01-01T00:00:01Z
	private String comment;//Comment for version 42.
	private String validityPeriodEnd;//2022-12-31T23:59:59Z
	private String pcfLegalStatement;//This PCF (Product Carbon Footprint) is for information purposes only. It is based upon the standards mentioned above.
	private String productDescription;//Ethanol, 95% solution
	private String precedingPfId;//3893bb5d-da16-4dc1-9185-11d97476c254private String  
    
    @Column(name = "shell_id")
    private String shellIdforPcf;
    @Column(name = "sub_model_id")
    private String subModelIdforPcf;
    @Column(name = "contract_defination_id")
    private String contractDefinationIdforPcf;
    @Column(name = "usage_policy_id")
    private String usagePolicyIdforPcf;
    @Column(name = "access_policy_id")
    private String accessPolicyIdforPcf;
    @Column(name = "asset_id")
    private String assetIdforPcf;
    @Column(name = "deleted")
    private String deletedforPcf;
    @Column(name = "updated")
    private String updatedforPcf;
}
