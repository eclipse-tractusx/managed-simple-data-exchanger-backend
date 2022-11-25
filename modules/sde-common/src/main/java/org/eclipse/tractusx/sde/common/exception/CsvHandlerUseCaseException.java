/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.common.exception;

public class CsvHandlerUseCaseException extends Exception {

	private final int rowPosition;

	private final int colomn;

	public CsvHandlerUseCaseException(int rowPosition, String message) {
		super(message);
		this.rowPosition = rowPosition;
		this.colomn = 0;
	}

	public CsvHandlerUseCaseException(int rowPosition, int colomn, String message) {
		super(message);
		this.rowPosition = rowPosition;
		this.colomn = colomn;
	}

	@Override
	public String getMessage() {
		return String.format("RowPosition: %s | Colomn: %s | Description: %s", rowPosition, colomn, super.getMessage());
	}
}
