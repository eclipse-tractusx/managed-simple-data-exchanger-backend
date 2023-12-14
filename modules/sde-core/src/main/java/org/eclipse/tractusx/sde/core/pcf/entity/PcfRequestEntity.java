package org.eclipse.tractusx.sde.core.pcf.entity;

import java.time.LocalDateTime;

import org.eclipse.tractusx.sde.common.enums.PCFRequestStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "pcf_requests_tbl")
@Entity
@Data
public class PcfRequestEntity {
	
	@Id
	@Column(name = "request_id")
	private String requestId;
	
	@Column(name = "product_id")
	private String productId;
	
	@Column(name = "bpn_number")
	private String bpnNumber;

	@Column(name = "message")
	private String message;
	
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private PCFRequestStatusEnum status;
	
	@Column(name = "requested_time")
	private LocalDateTime requestedTime;
	
	@Column(name = "last_updated_time")
	private LocalDateTime lastUpdatedTime;

}
