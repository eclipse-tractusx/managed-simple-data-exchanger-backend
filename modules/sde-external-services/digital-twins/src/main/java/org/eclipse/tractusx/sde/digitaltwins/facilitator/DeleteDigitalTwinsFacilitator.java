package org.eclipse.tractusx.sde.digitaltwins.facilitator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.DigitalTwinsFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

@Service
public class DeleteDigitalTwinsFacilitator {

	private static final String AUTHORIZATION = "Authorization";
	private static final String CLIENT_ID_TOKEN_QUERY_PARAMETER = "client_id";
	private static final String CLIENT_SECRET_TOKEN_QUERY_PARAMETER = "client_secret";
	private static final String GRANT_TYPE_TOKEN_QUERY_PARAMETER = "grant_type";
	private static final String ACCESS_TOKEN = "access_token";

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

	private final DigitalTwinsFeignClient digitalTwinsFeignClient;

	public DeleteDigitalTwinsFacilitator(DigitalTwinsFeignClient digitalTwinsFeignClient) {
		this.digitalTwinsFeignClient = digitalTwinsFeignClient;
	}

	public Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put(AUTHORIZATION, getBearerToken());

		return headers;
	}

	@SneakyThrows
	public void deleteDigitalTwinsById(String shellId) {
		try {
			digitalTwinsFeignClient.deleteDigitalTwinsById(shellId, getHeaders());
		} catch (Exception e) {
			parseExceptionMessage(e);
		}

	}

	@SneakyThrows
	public String getBearerToken() {

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
		String accessToken = node.path(ACCESS_TOKEN).asText();

		return "Bearer " + accessToken;
	}

	public void parseExceptionMessage(Exception e) throws ServiceException {

		if (!e.toString().contains("FeignException$NotFound") || !e.toString().contains("404 Not Found")) {
			throw new ServiceException("Exception in Digital delete request process: " + e.getMessage());
		}
	}
}
