/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.discovery;

import java.io.InputStream;

import org.eclipse.tractusx.sde.common.mapper.UsecaseMapper;
import org.eclipse.tractusx.sde.core.registry.UsecaseRegistration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class UseCaseDiscovery {

	private final UsecaseRegistration usecaseRegistration;

	private final UsecaseMapper usecaseMapper;

	@PostConstruct
	private void loadUsecase() {

		String resource = "use-case.json";
		// this is the path within the jar file
		InputStream input = this.getClass().getResourceAsStream("/resources/" + resource);
		if (input == null) {
			// this is how we load file within editor (eg eclipse)
			input = this.getClass().getClassLoader().getResourceAsStream(resource);
		}

		usecaseRegistration.register(usecaseMapper.jsonfileToJsonPojo(input));
	}

}
