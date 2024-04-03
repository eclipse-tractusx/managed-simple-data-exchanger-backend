package org.eclipse.tractusx.sde.pcfexchange.request;

import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcfRequestModel {

	private String requestId;
	
	@NotBlank
	private String productId;
	
	@NotBlank
	private String bpnNumber;
	
	private PCFTypeEnum type;
	private String message;
	private String remark;
	private PCFRequestStatusEnum status;
	private Long requestedTime;
	private Long lastUpdatedTime;
	

}