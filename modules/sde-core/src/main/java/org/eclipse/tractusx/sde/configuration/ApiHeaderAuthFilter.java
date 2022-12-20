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

package org.eclipse.tractusx.sde.configuration;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ApiHeaderAuthFilter extends GenericFilterBean {

	@Value("${dft.apiKeyHeader}")
	private String apiKeyHeader;

	@Value("${dft.apiKey}")
	private String apiKeyValue;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		var request = (HttpServletRequest) servletRequest;
		var response = (HttpServletResponse) servletResponse;

		String authHeaderValue = request.getHeader(apiKeyHeader);
		String url = request.getRequestURI();

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
		if (url.contains("/public") && !apiKeyValue.equals(authHeaderValue)) {
			log.error("**** ApiHeaderAuthFilter genreated Unauthorized response for public api *****************");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} else if(auth !=null && auth.getAuthorities()!=null && auth.getAuthorities().isEmpty()){
			log.error("**** ApiHeaderAuthFilter The resource/client is not allowed to access *****************");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}	else {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}
}