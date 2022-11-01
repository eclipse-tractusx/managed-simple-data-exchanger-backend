package com.catenax.sde.common.extensions;

import java.io.InputStream;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catenax.sde.common.mapper.SubmodelMapper;

import lombok.SneakyThrows;

@Component
public abstract class SubmodelExtension {

	@Autowired
	private SubmodelMapper submodelMapper;

	@SneakyThrows
	public JSONObject loadSubmodel(InputStream input) {
		return submodelMapper.jsonfileToJsonPojo(input);
	}

	public abstract JSONObject submodel();

}
