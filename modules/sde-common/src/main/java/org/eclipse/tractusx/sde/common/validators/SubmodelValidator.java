package org.eclipse.tractusx.sde.common.validators;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmodelValidator {

	private static final String DATE_PATTERN = "uuuu-M-d'T'HH:mm:ss";
	private static final String DATE_PATTERN_WITHZONE = "uuuu-M-d'T'HH:mm:ss.SSS'Z'";

	public boolean validateField(String ele, JsonObject jObject, JsonArray requiredFieldList, String value) {

		@SuppressWarnings("deprecation")
		JsonElement id2 = new JsonParser().parse(ele);
		if (requiredFieldList.contains(id2) && value == null)
			throw new ValidationException(String.format("'%s' This is required field", ele));
		else if (jObject.get("format") != null && jObject.get("format").getAsString().equals("date-time")) {
			try {
				if(value.endsWith("Z"))
					LocalDate.parse(value,
						DateTimeFormatter.ofPattern(DATE_PATTERN_WITHZONE).withResolverStyle(ResolverStyle.STRICT));
				else
					LocalDate.parse(value,
							DateTimeFormatter.ofPattern(DATE_PATTERN).withResolverStyle(ResolverStyle.STRICT));
			} catch (DateTimeParseException tecx) {
				throw new ValidationException(String.format("Field '%s' value '%s' is not valid", ele, value));
			}
		}

		if (jObject.get("pattern") != null && (value !=null && !Pattern.matches(jObject.get("pattern").getAsString(), value)))
			throw new ValidationException(String.format("Field '%s' value '%s' is not valid", ele, value));

		return true;

	}

}
