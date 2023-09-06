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
package org.eclipse.tractusx.sde.submodels.pcf.mapper;

import java.util.List;

import org.eclipse.tractusx.sde.common.mapper.AspectResponseFactory;
import org.eclipse.tractusx.sde.submodels.pcf.entity.PcfEntity;
import org.eclipse.tractusx.sde.submodels.pcf.model.CompanyIds;
import org.eclipse.tractusx.sde.submodels.pcf.model.CrossSectoralStandardsUsed;
import org.eclipse.tractusx.sde.submodels.pcf.model.DataQualityRating;
import org.eclipse.tractusx.sde.submodels.pcf.model.Pcf;
import org.eclipse.tractusx.sde.submodels.pcf.model.PcfAspect;
import org.eclipse.tractusx.sde.submodels.pcf.model.PcfSubmodelResponse;
import org.eclipse.tractusx.sde.submodels.pcf.model.PrecedingPfIds;
import org.eclipse.tractusx.sde.submodels.pcf.model.ProductIds;
import org.eclipse.tractusx.sde.submodels.pcf.model.ProductOrSectorSpecificRule;
import org.eclipse.tractusx.sde.submodels.pcf.model.ProductOrSectorSpecificRules;
import org.eclipse.tractusx.sde.submodels.pcf.model.SecondaryEmissionFactorSources;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class PcfMapper {
	
	@Value(value = "${manufacturerId}")
	private String manufacturerId;
	
	@Autowired
	private AspectResponseFactory aspectResponseFactory;
	
	ObjectMapper mapper = new ObjectMapper();

	@Mapping(target = "rowNumberforPcf", ignore = true)
	@Mapping(target = "subModelIdforPcf", ignore = true)
	public abstract PcfAspect mapFrom(PcfEntity aspect);

	public abstract PcfEntity mapFrom(PcfAspect aspect);

	@SneakyThrows
	public PcfAspect mapFrom(ObjectNode aspect) {
		return mapper.readValue(aspect.toString(), PcfAspect.class);
	}

	public PcfEntity mapforEntity(JsonObject aspect) {
		return new Gson().fromJson(aspect, PcfEntity.class);
	}

	public JsonObject mapFromEntity(PcfEntity aspect) {
		return new Gson().toJsonTree(aspect).getAsJsonObject();
	}

	public JsonObject mapToResponse(PcfEntity entity) {

		if (entity == null) {
			return null;
		}

		Pcf pcfResponse = Pcf.builder()
				.biogenicCarbonEmissionsOtherThanCO2(entity.getBiogenicCarbonEmissionsOtherThanCO2())
				.distributionStagePcfExcludingBiogenic(entity.getDistributionStagePcfExcludingBiogenic())
				.distributionStagePcfIncludingBiogenic(entity.getDistributionStagePcfIncludingBiogenic())
				.biogenicCarbonWithdrawal(entity.getBiogenicCarbonWithdrawal())
				.distributionStageBiogenicCarbonEmissionsOtherThanCO2(entity.getDistributionStageBiogenicCarbonEmissionsOtherThanCO2())
				.extWBCSDAllocationRulesDescription(entity.getExtWBCSDAllocationRulesDescription())
				.exemptedEmissionsDescription(entity.getExemptedEmissionsDescription())
				.distributionStageFossilGhgEmissions(entity.getDistributionStageFossilGhgEmissions())
				.exemptedEmissionsPercent(entity.getExemptedEmissionsPercent())
				.geographyCountrySubdivision(entity.getGeographyCountrySubdivision())
				.extTFSLuGhgEmissions(entity.getExtTFSLuGhgEmissions())
				.distributionStageBiogenicCarbonWithdrawal(entity.getDistributionStageBiogenicCarbonWithdrawal())
				.pcfIncludingBiogenic(entity.getPcfIncludingBiogenic())
				.aircraftGhgEmissions(entity.getAircraftGhgEmissions())
				.productMassPerDeclaredUnit(entity.getProductMassPerDeclaredUnit())
				.productOrSectorSpecificRules(List.of(ProductOrSectorSpecificRules.builder()
						.extWBCSDOperator(entity.getExtWBCSDOperator())
						.productOrSectorSpecificRulesObj(ProductOrSectorSpecificRule.builder()
								.ruleName(entity.getRuleName())
								.build())
						.extWBCSDOtherOperatorName(entity.getExtWBCSDOtherOperatorName())
						.build()))
				.extTFSAllocationWasteIncineration(entity.getExtTFSAllocationWasteIncineration())
				.pcfExcludingBiogenic(entity.getPcfExcludingBiogenic())
				.referencePeriodEnd(entity.getReferencePeriodEnd())
				.extWBCSDCharacterizationFactors(entity.getExtWBCSDCharacterizationFactors())
				.secondaryEmissionFactorSources(List.of(SecondaryEmissionFactorSources.builder()
						.secondaryEmissionFactorSource(entity.getSecondaryEmissionFactorSource())
						.build()))
				.unitaryProductAmount(entity.getUnitaryProductAmount())
				.declaredUnit(entity.getDeclaredUnit())
				.referencePeriodStart(entity.getReferencePeriodStart())
				.geographyRegionOrSubregion(entity.getGeographyRegionOrSubregion())
				.fossilGhgEmissions(entity.getFossilGhgEmissions())
				.extWBCSDPackagingGhgEmissions(entity.getExtWBCSDPackagingGhgEmissions())
				.boundaryProcessesDescription(entity.getBoundaryProcessesDescription())
				.geographyCountry(entity.getGeographyCountry())
				.extWBCSDPackagingEmissionsIncluded(Boolean.parseBoolean(entity.getExtWBCSDPackagingEmissionsIncluded()))
				.dlucGhgEmissions(entity.getDlucGhgEmissions())
				.carbonContentTotal(entity.getCarbonContentTotal())
				.extTFSDistributionStageLuGhgEmissions(entity.getExtTFSDistributionStageLuGhgEmissions())
				.extTFSDistributionStageDlucGhgEmissions(entity.getExtTFSDistributionStageDlucGhgEmissions())
				.primaryDataShare(entity.getPrimaryDataShare())
				.dataQualityRating(DataQualityRating.builder()
						.completenessDQR(entity.getCompletenessDQR())
						.technologicalDQR(entity.getTechnologicalDQR())
						.geographicalDQR(entity.getGeographicalDQR())
						.temporalDQR(entity.getTemporalDQR())
						.reliabilityDQR(entity.getReliabilityDQR())
						.coveragePercent(entity.getCoveragePercent())
						.build())
				.extWBCSDFossilCarbonContent(entity.getExtWBCSDFossilCarbonContent())
				.crossSectoralStandardsUsed(List.of(CrossSectoralStandardsUsed.builder()
						.crossSectoralStandard(entity.getCrossSectoralStandard())
						.build()))
				.extTFSAllocationWasteIncineration(entity.getExtTFSAllocationWasteIncineration())
				.carbonContentBiogenic(entity.getCarbonContentBiogenic())
				.build();
		
		PcfSubmodelResponse build = PcfSubmodelResponse.builder().specVersion(entity.getSpecVersion())
				.companyIds(CompanyIds.builder().companyId(entity.getCompanyId()).build())
				.extWBCSDProductCodeCpc(entity.getExtWBCSDProductCodeCpc()).created(entity.getCreated())
				.companyName(entity.getCompanyName()).extWBCSDPfStatus(entity.getExtWBCSDPfStatus())
				.version(entity.getVersion()).productName(entity.getProductName()).pcf(pcfResponse)
				.partialFullPcf(entity.getPartialFullPcf())
				.productIds(ProductIds.builder().productId(entity.getProductId()).build())

				.validityPeriodStart(entity.getValidityPeriodStart()).comment(entity.getComment()).id(entity.getId())
				.validityPeriodEnd(entity.getValidityPeriodEnd()).pcfLegalStatement(entity.getPcfLegalStatement())
				.productDescription(entity.getProductDescription())

				.precedingPfIds(PrecedingPfIds.builder().id(entity.getPrecedingPfId()).build()).build();

		PcfAspect csv = PcfAspect.builder().id(entity.getId()).specVersion(entity.getSpecVersion())
				.partialFullPcf(entity.getPartialFullPcf()).precedingPfId(entity.getPrecedingPfId())
				.version(entity.getVersion() + "").created(entity.getCreated())
				.extWBCSDPfStatus(entity.getExtWBCSDPfStatus()).validityPeriodStart(entity.getValidityPeriodStart())
				.validityPeriodEnd(entity.getValidityPeriodEnd()).comment(entity.getComment())
				.pcfLegalStatement(entity.getPcfLegalStatement()).companyName(entity.getCompanyName())
				.companyId(entity.getCompanyId()).productDescription(entity.getProductDescription())
				.productId(entity.getProductId()).extWBCSDProductCodeCpc(entity.getExtWBCSDProductCodeCpc())
				.productName(entity.getProductName()).declaredUnit(entity.getDeclaredUnit())
				.unitaryProductAmount(entity.getUnitaryProductAmount() + "")
				.productMassPerDeclaredUnit(entity.getProductMassPerDeclaredUnit() + "")
				.exemptedEmissionsPercent(entity.getExemptedEmissionsPercent() + "")
				.exemptedEmissionsDescription(entity.getExemptedEmissionsDescription())
				.extWBCSDPackagingEmissionsIncluded(
						Boolean.parseBoolean(entity.getExtWBCSDPackagingEmissionsIncluded()))
				.boundaryProcessesDescription(entity.getBoundaryProcessesDescription())
				.geographyCountrySubdivision(entity.getGeographyCountrySubdivision())
				.geographyCountry(entity.getGeographyCountry())
				.geographyRegionOrSubregion(entity.getGeographyRegionOrSubregion())
				.referencePeriodStart(entity.getReferencePeriodStart())
				.referencePeriodEnd(entity.getReferencePeriodEnd())
				.crossSectoralStandard(entity.getCrossSectoralStandard()).extWBCSDOperator(entity.getExtWBCSDOperator())
				.ruleName(entity.getRuleName()).extWBCSDOtherOperatorName(entity.getExtWBCSDOtherOperatorName())
				.extWBCSDCharacterizationFactors(entity.getExtWBCSDCharacterizationFactors())
				.extWBCSDAllocationRulesDescription(entity.getExtWBCSDAllocationRulesDescription())
				.extTFSAllocationWasteIncineration(entity.getExtTFSAllocationWasteIncineration())
				.primaryDataShare(entity.getPrimaryDataShare() + "")
				.secondaryEmissionFactorSource(entity.getSecondaryEmissionFactorSource())
				.coveragePercent(entity.getCoveragePercent() + "").technologicalDQR(entity.getTechnologicalDQR() + "")
				.temporalDQR(entity.getTemporalDQR() + "").geographicalDQR(entity.getGeographicalDQR() + "")
				.completenessDQR(entity.getCompletenessDQR() + "").reliabilityDQR(entity.getReliabilityDQR() + "")
				.pcfExcludingBiogenic(entity.getPcfExcludingBiogenic() + "")
				.pcfIncludingBiogenic(entity.getPcfIncludingBiogenic() + "")
				.fossilGhgEmissions(entity.getFossilGhgEmissions() + "")
				.biogenicCarbonEmissionsOtherThanCO2(entity.getBiogenicCarbonEmissionsOtherThanCO2() + "")
				.biogenicCarbonWithdrawal(entity.getBiogenicCarbonWithdrawal() + "")
				.dlucGhgEmissions(entity.getDlucGhgEmissions() + "")
				.extTFSLuGhgEmissions(entity.getExtTFSLuGhgEmissions() + "")
				.aircraftGhgEmissions(entity.getAircraftGhgEmissions() + "")
				.extWBCSDPackagingGhgEmissions(entity.getExtWBCSDPackagingGhgEmissions() + "")
				.distributionStagePcfExcludingBiogenic(entity.getDistributionStagePcfExcludingBiogenic() + "")
				.distributionStagePcfIncludingBiogenic(entity.getDistributionStagePcfIncludingBiogenic() + "")
				.distributionStageFossilGhgEmissions(entity.getDistributionStageFossilGhgEmissions() + "")
				.distributionStageBiogenicCarbonEmissionsOtherThanCO2(
						entity.getDistributionStageBiogenicCarbonEmissionsOtherThanCO2() + "")
				.distributionStageBiogenicCarbonWithdrawal(entity.getDistributionStageBiogenicCarbonWithdrawal() + "")
				.extTFSDistributionStageDlucGhgEmissions(entity.getExtTFSDistributionStageDlucGhgEmissions() + "")
				.extTFSDistributionStageLuGhgEmissions(entity.getExtTFSDistributionStageLuGhgEmissions() + "")
				.carbonContentTotal(entity.getCarbonContentTotal() + "")
				.extWBCSDFossilCarbonContent(entity.getExtWBCSDFossilCarbonContent() + "")
				.carbonContentBiogenic(entity.getCarbonContentBiogenic() + "")
				.assetLifeCyclePhase(entity.getAssetLifeCyclePhase()).build();
		
		return aspectResponseFactory.maptoReponse(csv, build);

	}

}
