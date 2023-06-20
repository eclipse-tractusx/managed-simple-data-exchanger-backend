/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.submodels.apr.steps;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.Endpoint;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.SemanticId;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubmodelDescriptionListResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.DigitalTwinGateway;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.eclipse.tractusx.sde.portal.handler.PortalProxyService;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalTwinsAspectRelationShipCsvHandlerUseCase extends Step {

	private final DigitalTwinGateway gateway;
	private final DigitalTwinsUtility digitalTwinsUtility;
	private final PortalProxyService portalProxyService;
	private final ConsumerControlPanelService consumerControlPanelService;

	@SneakyThrows
	public AspectRelationship run(AspectRelationship aspectRelationShip) throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(aspectRelationShip);
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					": DigitalTwins: " + e.getMessage());
		}
	}

	@SneakyThrows
	private AspectRelationship doRun(AspectRelationship aspectRelationShip)
			throws CsvHandlerUseCaseException, CsvHandlerDigitalTwinUseCaseException {

		ShellLookupRequest shellLookupRequest = getShellLookupRequest(aspectRelationShip);
		ShellLookupResponse shellIds = gateway.shellLookup(shellLookupRequest);

		String shellId = null;
		SubModelResponse foundSubmodel = null;

		if (shellIds.isEmpty()) {
			// We don't need to create parent shell from aspect relationship
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(), "No parent aspect found in DT");
		} else {
			foundSubmodel = checkShellforSubmodelExistorNot(aspectRelationShip, shellLookupRequest, shellIds,
					foundSubmodel);
		}

		shellId = aspectRelationShip.getShellId();
		CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(aspectRelationShip);

		if (foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			createSubModelSteps(aspectRelationShip, shellId, createSubModelRequest);
		} else {
			if (!foundSubmodel.getIdentification().equals(createSubModelRequest.getIdentification())) {
				gateway.deleteSubmodelfromShellById(shellId, foundSubmodel.getIdentification());
				createSubModelSteps(aspectRelationShip, shellId, createSubModelRequest);
				aspectRelationShip.setOldSubmodelIdforUpdateCase(foundSubmodel.getIdentification());
			}
			aspectRelationShip.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}

		return aspectRelationShip;
	}

	private SubModelResponse checkShellforSubmodelExistorNot(AspectRelationship aspectRelationShip,
			ShellLookupRequest shellLookupRequest, ShellLookupResponse shellIds, SubModelResponse foundSubmodel)
			throws CsvHandlerDigitalTwinUseCaseException {
		SubmodelDescriptionListResponse shellDescriptorWithsubmodelDetails = gateway
				.getShellDescriptorsWithSubmodelDetails(shellIds);

		List<String> submodelExistinceCount = new ArrayList<>();

		for (ShellDescriptorResponse shellDescriptorResponse : shellDescriptorWithsubmodelDetails.getItems()) {

			foundSubmodel = findMatchingSubmodel(aspectRelationShip, foundSubmodel, submodelExistinceCount,
					shellDescriptorResponse);
		}

		if (foundSubmodel == null && shellIds.size() > 1)
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple shell id's found for parent in DT %s", shellLookupRequest.toJsonString()));

		if (submodelExistinceCount.size() > 1)
			throw new CsvHandlerDigitalTwinUseCaseException(String.format(
					"%s submodel found multiple times in shells %s", getIdShortOfModel(), submodelExistinceCount));
		return foundSubmodel;
	}

	private SubModelResponse findMatchingSubmodel(AspectRelationship aspectRelationShip, SubModelResponse foundSubmodel,
			List<String> submodelExistinceCount, ShellDescriptorResponse shellDescriptorResponse) {
		aspectRelationShip.setShellId(shellDescriptorResponse.getIdentification());
		aspectRelationShip.setParentUuid(shellDescriptorResponse.getGlobalAssetId().getValue().get(0));

		for (SubModelResponse subModelResponse : shellDescriptorResponse.getSubmodelDescriptors()) {

			if (subModelResponse != null && getIdShortOfModel().equals(subModelResponse.getIdShort())) {
				aspectRelationShip.setSubModelId(subModelResponse.getIdentification());
				aspectRelationShip.setChildUuid(subModelResponse.getIdentification());
				foundSubmodel = subModelResponse;
				submodelExistinceCount.add(aspectRelationShip.getShellId());
			}
		}
		return foundSubmodel;
	}

	private void createSubModelSteps(AspectRelationship aspectRelationShip, String shellId,
			CreateSubModelRequest createSubModelRequest) {
		gateway.createSubModel(shellId, createSubModelRequest);
		aspectRelationShip.setSubModelId(createSubModelRequest.getIdentification());
		aspectRelationShip.setChildUuid(createSubModelRequest.getIdentification());
	}

	private ShellLookupRequest getShellLookupRequest(AspectRelationship aspectRelationShip) {
		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(CommonConstants.PART_INSTANCE_ID,
				aspectRelationShip.getParentPartInstanceId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_PART_ID,
				aspectRelationShip.getParentManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId());

		if (aspectRelationShip.hasOptionalParentIdentifier()) {
			shellLookupRequest.addLocalIdentifier(aspectRelationShip.getParentOptionalIdentifierKey(),
					aspectRelationShip.getParentOptionalIdentifierValue());
		}

		return shellLookupRequest;
	}

	private ShellLookupRequest getShellLookupRequestforChild(AspectRelationship aspectRelationShip) {
		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(CommonConstants.PART_INSTANCE_ID,
				aspectRelationShip.getChildPartInstanceId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_PART_ID,
				aspectRelationShip.getChildManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_ID,
				aspectRelationShip.getChildManufacturerId());

		if (aspectRelationShip.hasOptionalChildIdentifier()) {
			shellLookupRequest.addLocalIdentifier(aspectRelationShip.getChildOptionalIdentifierKey(),
					aspectRelationShip.getChildOptionalIdentifierValue());
		}

		return shellLookupRequest;
	}

	@SneakyThrows
	private CreateSubModelRequest getCreateSubModelRequest(AspectRelationship aspectRelationShip) {

		ArrayList<String> value = new ArrayList<>();
		value.add(getsemanticIdOfModel());
		ShellLookupRequest shellLookupRequest = getShellLookupRequestforChild(aspectRelationShip);

		List<ConnectorInfo> connectorEndpoints = portalProxyService
				.fetchConnectorInfo(List.of(aspectRelationShip.getChildManufacturerId()));

		List<String> dtURls = getDDTRUrl(connectorEndpoints);

		String childUUID = null;

		for (String ddtUrl : dtURls) {

			gateway.init(ddtUrl);

			ShellLookupResponse childshellIds = gateway.shellLookup(shellLookupRequest);

			if (childshellIds.isEmpty()) {
				log.warn(aspectRelationShip.getRowNumber() + ", " + ddtUrl + ", No child aspect found for "
						+ shellLookupRequest.toJsonString());
			}

			if (childshellIds.size() > 1) {
				log.warn(String.format("Multiple shell id's found for childAspect %s, %s", ddtUrl,
						shellLookupRequest.toJsonString()));
			}

			if (childshellIds.size() == 1) {
				SubmodelDescriptionListResponse shellDescriptorWithsubmodelDetails = gateway
						.getShellDescriptorsWithSubmodelDetails(childshellIds);

				for (ShellDescriptorResponse shellDescriptorResponse : shellDescriptorWithsubmodelDetails.getItems()) {
					childUUID = shellDescriptorResponse.getGlobalAssetId().getValue().get(0);
				}
			}

		}

		if (dtURls.isEmpty()) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					"No DTR registry found for child aspect look up");
		}

		if (childUUID == null) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					"No child aspect found for " + shellLookupRequest.toJsonString());
		}

		String identification = childUUID;
		SemanticId semanticId = new SemanticId(value);

		List<Endpoint> endpoints = digitalTwinsUtility.prepareDtEndpoint(aspectRelationShip.getShellId(),
				identification);

		return CreateSubModelRequest.builder().idShort(getIdShortOfModel()).identification(identification)
				.semanticId(semanticId).endpoints(endpoints).build();
	}

	private List<String> getDDTRUrl(List<ConnectorInfo> connectorInfos) {

		List<String> dtURls = new ArrayList<>();

		connectorInfos.stream().forEach(connectorInfo -> {
			connectorInfo.getConnectorEndpoint().parallelStream().distinct().forEach(connector -> {
				try {
					List<QueryDataOfferModel> queryDataOfferModel = consumerControlPanelService
							.queryOnDataOffers(connector, 10000, 0);

					log.info("For Connector " + connector + ", found asset :" + queryDataOfferModel.size());

					if (queryDataOfferModel != null && !queryDataOfferModel.isEmpty()) {

						List<String> list = queryDataOfferModel.stream()
								.filter(obj -> obj.getType().equals("data.core.digitalTwinRegistry"))
								.map(QueryDataOfferModel::getPublisher).toList();

						dtURls.addAll(list);
					}
				} catch (Exception e) {
					log.error("Error while looking EDC catalog for digitaltwin registry url, " + connector
							+ ", Exception :" + e.getMessage());
				}
			});
		});

		return dtURls;
	}

}