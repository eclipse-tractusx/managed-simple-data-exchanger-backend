package org.eclipse.tractusx.sde.pcfexchange.request;

import java.time.LocalDateTime;

import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;

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
public class PcfRequestModel {

	private String requestId;
	private String productId;
	private String bpnNumber;
	private PCFTypeEnum type;
	private String message;
	private PCFRequestStatusEnum status;
	private LocalDateTime requestedTime;
	private LocalDateTime lastUpdatedTime;

}
