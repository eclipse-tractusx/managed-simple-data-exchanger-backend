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

package org.eclipse.tractusx.sde.pcfexchange.enums;

public enum PCFRequestStatusEnum {
    REQUESTED,
    APPROVED,
    
    PUSHING_DATA,
    PUSHING_UPDATED_DATA,
    
    REJECTED,
    SENDING_REJECT_NOTIFICATION,
    
	PUSHED,
	PUSHED_UPDATED_DATA,
	
	RECEIVED,
    FAILED_TO_PUSH_DATA, 
    FAILED_TO_SEND_REJECT_NOTIFICATION, 
    FAILED, 
    
    SENDING_REQUEST, 
    PENDING_DATA_FROM_PROVIDER
}
