package org.eclipse.tractusx.sde.common.validators;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

@Service
public class SubmodelCSVValidator {

	public boolean validate(JsonObject asJsonObject, List<String> columns, String submodel) {

		Set<String> keySet = asJsonObject.keySet();
		Set<String> targetSet = new LinkedHashSet<>(columns);
		if (!keySet.equals(targetSet))
			throw new ValidationException(String.format("Csv coloumn header is not matching %s submodel", submodel));

		return true;

	}

}
