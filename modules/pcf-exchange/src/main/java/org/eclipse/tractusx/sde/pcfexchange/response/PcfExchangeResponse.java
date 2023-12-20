package org.eclipse.tractusx.sde.pcfexchange.response;

import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFOptionsEnum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcfExchangeResponse {

	private String message;
	private PCFOptionsEnum status;
	private ShellDescriptorResponse shellDescriptorResponse;

}
