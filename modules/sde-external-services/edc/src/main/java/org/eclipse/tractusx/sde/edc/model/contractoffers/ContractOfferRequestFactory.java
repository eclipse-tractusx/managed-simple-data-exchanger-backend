package org.eclipse.tractusx.sde.edc.model.contractoffers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.SneakyThrows;

@Component
public class ContractOfferRequestFactory {

	@SneakyThrows
	public ObjectNode getContractOfferRequest(String providerUrl, Integer limit, Integer offset,
			String filterExpression) {

		if (!StringUtils.isBlank(filterExpression))
			filterExpression = String.format(", %s", filterExpression);
		else
			filterExpression = "";
		
		String formatSchema = """
				{
				 "@context": {},
				 "protocol": "dataspace-protocol-http",
				 "providerUrl": "%s",
				 "querySpec": {
				 "offset": %s,
				 "limit": %s
				 %s
				 }
				}
				""";
		String jsonString = String.format(formatSchema, providerUrl, offset, limit,
				filterExpression);

		return (ObjectNode) new ObjectMapper().readTree(jsonString);
	}

}
