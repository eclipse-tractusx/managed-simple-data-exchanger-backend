package com.catenax.sde.common.mapper;

import java.io.InputStream;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubmodelMapper {

	default JSONObject jsonfileToJsonPojo(InputStream input) {
		JSONTokener tokenizer = new JSONTokener(input);
		return new JSONObject(tokenizer);
	}

}
