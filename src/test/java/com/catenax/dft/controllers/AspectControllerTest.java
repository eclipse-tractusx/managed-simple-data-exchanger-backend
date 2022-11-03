package com.catenax.dft.controllers;

import com.catenax.dft.entities.SubmodelJsonRequest;

import com.catenax.dft.entities.UsagePolicy;
import com.catenax.dft.entities.aspect.*;
import com.catenax.dft.entities.aspectrelationship.AspectRelationshipRequest;
import com.catenax.dft.entities.aspectrelationship.AspectRelationshipResponse;

import com.catenax.dft.enums.PolicyAccessEnum;
import com.catenax.dft.enums.UsagePolicyEnum;
import com.catenax.dft.usecases.aspectrelationship.GetAspectsRelationshipUseCase;
import com.catenax.dft.usecases.aspects.GetAspectsUseCase;
import com.catenax.dft.usecases.csvhandler.aspectrelationship.CreateAspectRelationshipUseCase;
import com.catenax.dft.usecases.csvhandler.aspects.CreateAspectsUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AspectControllerTest {
    @InjectMocks
    AspectController aspectController;
    @Mock
    private GetAspectsUseCase aspectsUseCase;
    @Mock
    private CreateAspectsUseCase createAspectsUseCase;
    @Mock
    private GetAspectsRelationshipUseCase aspectsRelationshipUseCase;
    @Mock
    private CreateAspectRelationshipUseCase createAspectRelationshipUseCase;

    @Test
    void getAspect() {

        AspectResponse aspectResponse =  getAspectResponseObject();
        String uuid = "urn:uuid:580d3adf-1981-2022-a214-13d6ceed9379";
        when(aspectsUseCase.execute(uuid)).thenReturn(aspectResponse);
        ResponseEntity<AspectResponse> result = aspectController.getAspect(uuid);
        assertThat(result.getBody()).isEqualTo(aspectResponse);
    }

    @Test
    void getAspectNotFound() {

        AspectResponse  aspectResponse = null;
        String uuid = "urn:uuid:580d3adf-1981-2022-a214-13d6ceed93";
        when(aspectsUseCase.execute(uuid)).thenReturn(null);
        ResponseEntity<AspectResponse> result = aspectController.getAspect(uuid);
        assertThat(result.getBody()).isEqualTo(aspectResponse);
    }

    //@Test
    void getAspectRelationships() {

        String  response = "{\"catenaXId\":\"urn:uuid:0ece43f1-7bd0-45e8-b404-ecad88e06565\",\"childParts\":[{\"lifecycleContext\":\"pdf\",\"quantity\":{\"quantityNumber\":1234.0,\"measurementUnit\":{\"lexicalValue\":\"pdf\",\"datatypeURI\":\"pdf\"}},\"assembledOn\":\"pdf\",\"lastModifiedOn\":null,\"childCatenaXId\":\"urn:uuid:95c8fd91-3345-4e91-8d5a-c6dd9876700ba\"}]}";
        try {
            AspectRelationshipResponse aspectRelationshipResponse = new ObjectMapper().readValue(response, AspectRelationshipResponse.class);
            String uuid = "urn:uuid:0ece43f1-7bd0-45e8-b404-ecad88e06565";
            when(aspectsRelationshipUseCase.execute(uuid)).thenReturn(aspectRelationshipResponse);
            ResponseEntity<AspectRelationshipResponse> result = aspectController.getAspectRelationships(uuid);
            assertThat(result.getBody()).isEqualTo(aspectRelationshipResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getAspectRelationshipsNotFound() {

        AspectRelationshipResponse aspectRelationshipResponse = null;
        String uuid = "urn:uuid:0ece43f1-7bd0-45e8-b404-ecad88e06";
        when(aspectsRelationshipUseCase.execute(uuid)).thenReturn(null);
        ResponseEntity<AspectRelationshipResponse> result = aspectController.getAspectRelationships(uuid);
        assertThat(result.getBody()).isEqualTo(aspectRelationshipResponse);

    }

    @Test
    void createAspect() {
        SubmodelJsonRequest<AspectRequest> aspectRequest = getAspectRequest();
        ResponseEntity<String> result = aspectController.createAspect(aspectRequest);
        assertThat(result.getBody()).isNotEmpty();
    }

    @Test
    void createAspectRelationship() {
        SubmodelJsonRequest<AspectRelationshipRequest> aspectsRelationshipRequest = getAspectRelationshipRequest();
        ResponseEntity<String> result = aspectController.createAspectRelationship(aspectsRelationshipRequest);
        assertThat(result.getBody()).isNotEmpty();
    }

    private SubmodelJsonRequest<AspectRequest> getAspectRequest() {

        String typeOfAccess = "restricted";
        List<String> bpnNumber = Stream.of("BPNL00000005CONS","BPNL00000005PROV").toList();
        List<UsagePolicy> list = new ArrayList<>();
        UsagePolicy UsagePolicy = com.catenax.dft.entities.UsagePolicy.builder()
                .type(UsagePolicyEnum.ROLE).typeOfAccess(PolicyAccessEnum.RESTRICTED).value("ADMIN").build();
        list.add(UsagePolicy);
        List<AspectRequest> rowData = new ArrayList<>();
        AspectRequest aspect = AspectRequest.builder()
                .uuid("urn:uuid:580d3adf-1981-2022-a214-13d6ccmp9889")
                .partInstanceId("")
                .optionalIdentifierKey(null)
                .optionalIdentifierValue(null)
                .manufacturingDate("2022-02-06T14:48:54")
                .manufacturingCountry("HUR")
                .manufacturerPartId("123-0.740-3433-C")
                .customerPartId("PRT-NO347")
                .classification("product")
                .nameAtManufacturer("Mirror left")
                .nameAtCustomer("side element B")
                .build();
        rowData.add(aspect);

        return  new SubmodelJsonRequest<AspectRequest>(rowData, typeOfAccess, bpnNumber, list);
    }

    private AspectResponse getAspectResponseObject() {

        List<LocalIdentifier> identifiersList = new ArrayList<>();
        LocalIdentifier identifierOne = new LocalIdentifier("AspectID","BID12345678");
        LocalIdentifier identifierTwo = new LocalIdentifier("ManufacturerPartID","123-0.740-3434-A");
        LocalIdentifier identifierThree = new LocalIdentifier("ManufacturerID","catenaX");
        identifiersList.add(identifierOne);
        identifiersList.add(identifierTwo);
        identifiersList.add(identifierThree);

        ManufacturingInformation manufacturInfo = ManufacturingInformation.builder()
                .country("HUR")
                .date("2022-02-04T14:48:54")
                .build();
        PartTypeInformation partType = PartTypeInformation.builder()
                .manufacturerPartID("123-0.740-3434-A")
                .customerPartId("PRT-12345")
                .classification("product")
                .nameAtManufacturer("Mirror left")
                .nameAtCustomer("side element A")
                .build();

        return AspectResponse.builder()
                .catenaXId("urn:uuid:580d3adf-1981-2022-a214-13d6ceed9379")
                .localIdentifiers(identifiersList)
                .manufacturingInformation(manufacturInfo)
                .partTypeInformation(partType)
                .build();
    }

    private SubmodelJsonRequest<AspectRelationshipRequest> getAspectRelationshipRequest() {

        String typeOfAccess = "restricted";
        List<String> bpnNumber = Stream.of("BPNL00000005CONS","BPNL00000005PROV").toList();
        List<UsagePolicy> list = new ArrayList<>();
        UsagePolicy UsagePolicy = com.catenax.dft.entities.UsagePolicy.builder()
                .type(UsagePolicyEnum.ROLE).typeOfAccess(PolicyAccessEnum.RESTRICTED).value("ADMIN").build();
        list.add(UsagePolicy);
        List<AspectRelationshipRequest> rowData = new ArrayList<>();
        AspectRelationshipRequest aspectRelationshipRequest = AspectRelationshipRequest.builder()
                .childUuid("urn:uuid:95c8fd91-3345-4e91-8d5a-c6dd998700ba")
                .childPartInstanceId("PII-002")
                .childManufacturerPartId("123-0.740-3433-C")
                .childOptionalIdentifierKey(null)
                .childOptionalIdentifierValue(null)
                .parentUuid("urn:uuid:0ece43f1-7bd0-45e8-b404-ecad99e0ddea")
                .parentPartInstanceId("tere")
                .parentManufacturerPartId("rerer")
                .parentOptionalIdentifierKey(null)
                .parentOptionalIdentifierValue(null)
                .lifecycleContext("pdf")
                .quantityNumber("1234")
                .measurementUnitLexicalValue("pdf")
                .dataTypeUri("pdf")
                .assembledOn("pdf")
                .build();
        rowData.add(aspectRelationshipRequest);

        return  new SubmodelJsonRequest<AspectRelationshipRequest>(rowData, typeOfAccess, bpnNumber, list);

    }

}