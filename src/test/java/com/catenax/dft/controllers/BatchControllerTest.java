package com.catenax.dft.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.catenax.dft.entities.SubmodelJsonRequest;
import com.catenax.dft.entities.aspect.LocalIdentifier;
import com.catenax.dft.entities.aspect.ManufacturingInformation;
import com.catenax.dft.entities.aspect.PartTypeInformation;
import com.catenax.dft.entities.batch.BatchRequest;
import com.catenax.dft.entities.batch.BatchResponse;
import com.catenax.dft.usecases.batchs.GetBatchsUseCase;
import com.catenax.dft.usecases.csvhandler.batchs.CreateBatchsUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@ExtendWith(MockitoExtension.class)
class BatchControllerTest {

	@InjectMocks
	BatchController batchController;

	@Mock
	private GetBatchsUseCase batchsUseCase;
	
	@Mock
	private CreateBatchsUseCase createBatchsUseCase;

	@Test
	void testGetBatchAspectSuccess() throws JsonMappingException, JsonProcessingException {
			
		BatchResponse batchResponse =  getBatchResponseObject();
		String uuid = "urn:uuid:580d3adf-1981-2022-a214-13d6ceed9379";
		when(batchsUseCase.execute(uuid)).thenReturn(batchResponse);

		ResponseEntity<BatchResponse> result = batchController.getBatch(uuid);

		assertThat(result.getBody()).isEqualTo(batchResponse);

	}
	
	@Test
	void testGetBatchAspectNotFound() throws JsonMappingException, JsonProcessingException {
		
		BatchResponse  batchResponse = null;
		String uuid = "urn:uuid:580d3adf-1981-2022-a214-13d6ceed93";
		when(batchsUseCase.execute(uuid)).thenReturn(null);

		ResponseEntity<BatchResponse> result = batchController.getBatch(uuid);

		assertThat(result.getBody()).isEqualTo(batchResponse);
	}
	
	
	@Test
	void testCreateBatchAspectSuccess() throws JsonMappingException, JsonProcessingException {
		
		SubmodelJsonRequest<BatchRequest> batchAspectsRequest = getBatchRequest();
		ResponseEntity<String> result = batchController.createAspectBatch(batchAspectsRequest);
		assertThat(result.getBody()).isNotEmpty();

	}
	
	
	private SubmodelJsonRequest<BatchRequest> getBatchRequest() {
		
		String typeOfAccess = "restricted";
		List<String> bpnNumber = Stream.of("BPNL00000005CONS","BPNL00000005PROV").toList();
		List<BatchRequest> rowData = new ArrayList<>();
		BatchRequest batch = BatchRequest.builder()
				.uuid("urn:uuid:580d3adf-1981-2022-a214-13d6ccmp9889")
				.batchId("")
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
		rowData.add(batch);
				
	return  new SubmodelJsonRequest<BatchRequest>(rowData, typeOfAccess, bpnNumber);
	}

	private BatchResponse getBatchResponseObject() {
		
		List<LocalIdentifier> identifiersList = new ArrayList<>();
		LocalIdentifier identifierOne = new LocalIdentifier("BatchID","BID12345678");
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
		
		return BatchResponse.builder()
				.catenaXId("urn:uuid:580d3adf-1981-2022-a214-13d6ceed9379")
				.localIdentifiers(identifiersList)
				.manufacturingInformation(manufacturInfo)
				.partTypeInformation(partType)
				.build();
	}


}
