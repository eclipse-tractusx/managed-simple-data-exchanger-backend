package com.catenax.sde.usecases.csvhandler.delete;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.catenax.sde.digitaltwins.entities.request.ShellLookupRequest;
import com.catenax.sde.digitaltwins.gateways.external.DigitalTwinsFeignClient;
import com.catenax.sde.edc.api.EDCFeignClientApi;
import com.catenax.sde.entities.database.AspectEntity;
import com.catenax.sde.entities.database.FailureLogEntity;
import com.catenax.sde.enums.CsvTypeEnum;
import com.catenax.sde.exceptions.DftException;
import com.catenax.sde.gateways.database.AspectRepository;
import com.catenax.sde.usecases.logs.FailureLogsUseCase;
import com.catenax.sde.usecases.processreport.ProcessReportUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeleteUsecaseHandler {
	
	
	//Shell Request
	
    private static final String AUTHORIZATION = "Authorization";
    private static final String ASSET_IDS_QUERY_PARAMETER = "assetIds";
    private static final String CLIENT_ID_TOKEN_QUERY_PARAMETER = "client_id";
    private static final String CLIENT_SECRET_TOKEN_QUERY_PARAMETER = "client_secret";
    private static final String GRANT_TYPE_TOKEN_QUERY_PARAMETER = "grant_type";
    private static final String ACCESS_TOKEN = "access_token";
    
    private static final String DELETED_Y = "Y";
    private static final String PART_INSTANCE_ID = "PartInstanceID";
    private static final String MANUFACTURER_PART_ID = "ManufacturerPartID";
    private static final String MANUFACTURER_ID = "ManufacturerID";
    
    @Value(value = "${digital-twins.authentication.clientSecret}")
    private String clientSecret;
    @Value(value = "${digital-twins.authentication.clientId}")
    private String clientId;
    @Value(value = "${digital-twins.authentication.grantType}")
    private String grantType;
    @Value(value = "${digital-twins.hostname}")
    private String digitalTwinsHost;
    @Value(value = "${digital-twins.authentication.url}")
    private String tokenUrl;
    
    @Value(value = "${edc.apiKeyHeader}")
    private String apiKeyHeader;
    @Value(value = "${edc.apiKey}")
    private String apiKey;
    
    private String accessToken;
    
    private int deletedRecordCount=0;
    
    
	@Autowired
	DigitalTwinsFeignClient digitalTwinsFeignClient;
	@Autowired
	EDCFeignClientApi eDCFeignClientApi;
    @Autowired
    private FailureLogsUseCase failureLogsUseCase;
	
    private final ProcessReportUseCase processReportUseCase;
	
    AspectRepository aspectRepository;
	
    
    public DeleteUsecaseHandler(ProcessReportUseCase processReportUseCase,AspectRepository aspectRepository) {
		super();
		this.processReportUseCase = processReportUseCase;
		this.aspectRepository =aspectRepository;
	}
    
    
	public String deleteAspectDigitalTwinsAndEDC(final List<AspectEntity> listOfAspectIds, String refProcessId)
			throws JsonProcessingException {

		/*
		 * IMP NOTE: in delete case existing processid will be stored into
		 * referenceProcessid
		 */
		String processId = UUID.randomUUID().toString();
		processReportUseCase.startDeleteProcess(processId, CsvTypeEnum.ASPECT, listOfAspectIds.size(), refProcessId, 0);

		listOfAspectIds.parallelStream().forEach((o) -> {
			deleteAllDataBySequence(o);
		});

		processReportUseCase.finalizeNoOfDeletedInProgressReport(processId, deletedRecordCount, refProcessId);

		return processId;
	}
	
	private void deleteAllDataBySequence(AspectEntity aspectEntity) {
		String processId = aspectEntity.getProcessId();
		String assetId = aspectEntity.getAssetId();
		try {
			ResponseEntity<Object> response = digitalTwinsFeignClient.deleteDigitalTwinsById(aspectEntity.getShellId(),
					setHeaders());
			if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
				ResponseEntity<Object> contractDefinationResponse = eDCFeignClientApi
						.deleteContractDefinition(aspectEntity.getContractDefinationId(), setEDCHeaders());
				if (contractDefinationResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
					ResponseEntity<Object> acessPolicyResponse = eDCFeignClientApi
							.deletePolicyDefinitions(aspectEntity.getAccessPolicyId(), setEDCHeaders());
					ResponseEntity<Object> usagePolicyResponse = eDCFeignClientApi
							.deletePolicyDefinitions(aspectEntity.getUsagePolicyId(), setEDCHeaders());
					if (acessPolicyResponse.getStatusCode() == HttpStatus.NO_CONTENT
							&& usagePolicyResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
						ResponseEntity<Object> assetResponse = eDCFeignClientApi.deleteAssets(assetId, setEDCHeaders());
						if (assetResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
							saveAspectWithDeleted(aspectEntity);

						}
					}
				}
			}
		} catch (Exception e) {

			FailureLogEntity entity = FailureLogEntity.builder().uuid(UUID.randomUUID().toString()).processId(processId)
					.log(e.getMessage()).dateTime(LocalDateTime.now()).build();
			failureLogsUseCase.saveLog(entity);
			log.error(String.format("[%s] %s", this.getClass().getSimpleName(), String.valueOf(e)));
		}

	}
	
	private void saveAspectWithDeleted(AspectEntity aspectEntity) {
		aspectEntity.setDeleted(DELETED_Y);
		aspectRepository.save(aspectEntity);
		++deletedRecordCount;
	}
	
	private Map<String, String> setHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put(AUTHORIZATION, getBearerToken());

		return headers;
	}
	
	private HttpHeaders setEDCHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(apiKeyHeader, apiKey);
		return headers;
	}
	
	
	 @SneakyThrows
	    private String getBearerToken() {
	       
	        RestTemplate restTemplate = new RestTemplate();
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
	        map.add(CLIENT_ID_TOKEN_QUERY_PARAMETER, clientId);
	        map.add(CLIENT_SECRET_TOKEN_QUERY_PARAMETER, clientSecret);
	        map.add(GRANT_TYPE_TOKEN_QUERY_PARAMETER, grantType);

	        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
	        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, entity, String.class);

	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode node = mapper.readTree(response.getBody());
	        accessToken = node.path(ACCESS_TOKEN).asText();

	        return "Bearer " + accessToken;
	    }
	 



}
