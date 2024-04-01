/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.pcfexchange.entity;

import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;

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
	
	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private PCFTypeEnum type;

	@Column(name = "message" , columnDefinition = "TEXT")
	private String message;
	
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private PCFRequestStatusEnum status;
	
	@Column(name = "requested_time")
	private Long requestedTime;
	
	@Column(name = "last_updated_time")
	private Long lastUpdatedTime;
	
	@Column(name = "remark" , columnDefinition = "TEXT")
	private String remark;

}
