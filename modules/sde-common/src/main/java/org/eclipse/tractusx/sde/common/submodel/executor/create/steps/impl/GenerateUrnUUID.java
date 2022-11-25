package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.SneakyThrows;

@Component
public class GenerateUrnUUID extends Step {

	@SneakyThrows
	public ObjectNode run(ObjectNode jsonObject, String processId) {

		String uUID = jsonObject.get("uuid").asText();
		if (uUID == null || uUID.isBlank() || uUID.equals("null")) {
			jsonObject.put("uuid", UUIdGenerator.getUrnUuid());
		} else if (!uUID.startsWith(UUIdGenerator.URN_UUID_PREFIX)) {
			String concat = UUIdGenerator.URN_UUID_PREFIX.concat(uUID);
			jsonObject.put("uuid", concat);
		}
		return jsonObject;
	}

}
