package com.catenax.sde.core.validation;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.catenax.sde.common.exception.ValidationException;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SumodelValidator {

	public boolean validate(JsonObject rowjObj, JsonObject asJsonObject, JsonObject requiredFieldList) {
		Set<String> keySet = rowjObj.keySet();

		keySet.forEach(ele -> {

			JsonObject jObject = asJsonObject.get(ele).getAsJsonObject();

			if (requiredFieldList.get(ele) != null && rowjObj.get(ele) == null)
				throw new ValidationException(String.format("'%s' This is required field", ele));

			else if (jObject.get("pattent") != null
					&& !Pattern.matches(jObject.get("pattent").getAsString(), rowjObj.get(ele).getAsString()))

				throw new ValidationException(
						String.format("Field '%s' value '%s' is not valid", ele, rowjObj.get(ele).getAsString()));

		});
		return true;

	}

}
