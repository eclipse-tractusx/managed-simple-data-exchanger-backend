package com.catenax.sde.common.extensions;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catenax.sde.common.mapper.SubmodelMapper;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Component
public abstract class SubmodelExtension {

	@Autowired
	private SubmodelMapper submodelMapper;

	@SneakyThrows
	public JsonObject loadSubmodel(InputStream input) {
		return submodelMapper.jsonfileToJsonPojo(input);
	}

	public abstract JsonObject submodel();

}
