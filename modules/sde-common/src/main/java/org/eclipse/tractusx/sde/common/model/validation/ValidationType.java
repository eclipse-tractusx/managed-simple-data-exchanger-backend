package org.eclipse.tractusx.sde.common.model.validation;

public enum ValidationType {

	REQUIRED("", "The '{}' field should be required"),

	ALPHANUMERIC("[a-zA-Z0-9]", "The '{}' field should be required"),

	NOT_BLANK("", "The '{}' field should not be blank"),

	NUMBER("", "The '{}' field should be number"),

	DATE("", "The '{}' field should be date in {}"),

	DATE_TIMESTAMP("", "The '{}' field should be date timestamp in {}");

	String defaultPattern;
	String defaultErrorMsg;

	private ValidationType(String defaultPattern, String defaultErrorMsg) {

		this.defaultPattern = defaultPattern;
		this.defaultErrorMsg = defaultErrorMsg;
	}
}
