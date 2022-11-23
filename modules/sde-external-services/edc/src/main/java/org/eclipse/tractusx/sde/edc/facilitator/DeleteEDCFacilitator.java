package org.eclipse.tractusx.sde.edc.facilitator;

import org.eclipse.tractusx.sde.edc.api.EDCFeignClientApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

@Service
public class DeleteEDCFacilitator {

	@Value(value = "${edc.apiKeyHeader}")
	private String apiKeyHeader;
	@Value(value = "${edc.apiKey}")
	private String apiKey;

	private final EDCFeignClientApi eDCFeignClientApi;

	public DeleteEDCFacilitator(EDCFeignClientApi eDCFeignClientApi) {
		this.eDCFeignClientApi = eDCFeignClientApi;
	}

	public HttpHeaders getEDCHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(apiKeyHeader, apiKey);
		return headers;
	}
	
	@SneakyThrows
	public void deleteContractDefination(String contractDefinationId) {
		try {
			eDCFeignClientApi.deleteContractDefinition(contractDefinationId, getEDCHeaders());
		} catch (Exception e) {
			parseExceptionMessage(e);
		}

	}
	
	@SneakyThrows
	public void deleteAccessPolicy(String accessPolicyId) {
		try {
			eDCFeignClientApi.deletePolicyDefinitions(accessPolicyId, getEDCHeaders());
		} catch (Exception e) {
			parseExceptionMessage(e);
		}

	}
	
	@SneakyThrows
	public void deleteUsagePolicy(String usagePolicyId) {
		try {
			eDCFeignClientApi.deletePolicyDefinitions(usagePolicyId, getEDCHeaders());
		} catch (Exception e) {
			parseExceptionMessage(e);
		}

	}

	@SneakyThrows
	public void deleteAssets(String assetId) {
		try {
			eDCFeignClientApi.deleteAssets(assetId, getEDCHeaders());
		} catch (Exception e) {
			throw new Exception("Exception in EDC delete request process:"+e.getMessage());
		}

	}

	
	private void parseExceptionMessage(Exception e) throws Exception {

		if (!e.toString().contains("FeignException$NotFound") || !e.toString().contains("404 Not Found")) {
			throw new Exception("Exception in EDC delete request process");
		}
	}
}
