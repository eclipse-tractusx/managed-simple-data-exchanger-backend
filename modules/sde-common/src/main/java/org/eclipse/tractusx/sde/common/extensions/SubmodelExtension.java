package org.eclipse.tractusx.sde.common.extensions;

import java.io.InputStream;

import org.eclipse.tractusx.sde.common.mapper.SubmodelMapper;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Component
public abstract class SubmodelExtension {

	@Autowired
	private SubmodelMapper submodelMapper;

	@SneakyThrows
	public Submodel loadSubmodel(InputStream input) {
		JsonObject schema = submodelMapper.jsonfileToJsonPojo(input);
		
		return Submodel.builder()
				.id(schema.get("id").getAsString())
				.name(schema.get("items").getAsJsonObject().get("title").getAsString())
				.schema(schema).build();
	}

	public abstract Submodel submodel();

}
