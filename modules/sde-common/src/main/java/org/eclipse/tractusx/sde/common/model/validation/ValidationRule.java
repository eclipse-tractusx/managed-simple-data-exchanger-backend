package org.eclipse.tractusx.sde.common.model.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationRule {

	private ValidationType validation;
	private String regPattern;
	private String errorMsg;

}
