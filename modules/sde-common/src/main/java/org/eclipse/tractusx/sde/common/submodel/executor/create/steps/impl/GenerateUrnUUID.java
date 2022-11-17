package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Component
public class GenerateUrnUUID extends Step {

	@SneakyThrows
	public JsonObject run(JsonObject jsonObject, String processId) {

		String uUID = jsonObject.get("uuid").getAsString();
		if (uUID == null || uUID.isBlank()) {
<<<<<<< HEAD
			jsonObject.put("uuid", UUIdGenerator.getUrnUuid());
=======
			jsonObject.addProperty("uuid", UUIdGenerator.getUrnUuid());
>>>>>>> branch 'Modules-branch_PartAsPlanned' of https://github.com/catenax-ng/product-dft-backend.git
		} else if (!uUID.startsWith(UUIdGenerator.URN_UUID_PREFIX)) {
			String concat = UUIdGenerator.URN_UUID_PREFIX.concat(uUID);
<<<<<<< HEAD
			jsonObject.put("uuid", concat);
=======
			jsonObject.addProperty("uuid", concat);
>>>>>>> branch 'Modules-branch_PartAsPlanned' of https://github.com/catenax-ng/product-dft-backend.git
		}
		return jsonObject;
	}

}
