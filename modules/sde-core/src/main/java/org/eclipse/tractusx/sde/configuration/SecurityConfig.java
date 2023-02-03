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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private static final String[] PUBLIC_URL = { "/ping", "/*/public/**", "/api-docs/**", "/swagger-ui/**",
			"*/swagger-ui/**", "/actuator/health/readiness", "/actuator/health/liveness", "/v3/api-docs/**" };

	@Value("${keycloak.resource.clientid}")
	private String resourceName;

	public interface Jwt2AuthoritiesConverter extends Converter<Jwt, Collection<? extends GrantedAuthority>> {
	}

	@SuppressWarnings("unchecked")
	@Bean
	public Jwt2AuthoritiesConverter authoritiesConverter() {
		// This is a converter for roles as embedded in the JWT by a Keycloak server
		// Roles are taken from both realm_access.roles & resource_access.{client}.roles

		return jwt -> {
			final var realmAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", Map.of());
			final var realmRoles = (Collection<String>) realmAccess.getOrDefault("roles", List.of());

			final var resourceAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("resource_access", Map.of());
			// read all roles from resource
			final var confidentialClientAccess = (Map<String, Object>) resourceAccess.getOrDefault(resourceName,
					Map.of());
			final var confidentialClientRoles = (Collection<String>) confidentialClientAccess.getOrDefault("roles",
					List.of());

			return Stream.concat(realmRoles.stream(), confidentialClientRoles.stream()).map(SimpleGrantedAuthority::new)
					.toList();
		};
	}

	public interface Jwt2AuthenticationConverter extends Converter<Jwt, AbstractAuthenticationToken> {
	}

	@Bean
	public Jwt2AuthenticationConverter authenticationConverter(Jwt2AuthoritiesConverter authoritiesConverter) {
		return jwt -> new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt));
	}

	@SneakyThrows
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, Jwt2AuthenticationConverter authenticationConverter,
			ServerProperties serverProperties) {

		// Enable OAuth2 with custom authorities mapping
		http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(authenticationConverter);

		// Enable anonymous
		http.anonymous();

		// Enable and configure CORS
		http.cors().configurationSource(request -> {
			CorsConfiguration cors = new CorsConfiguration();
			cors.setAllowedOrigins(Arrays.asList("*"));
			cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
			cors.setAllowedHeaders(Arrays.asList("*"));
			return cors;
		});

		// State-less session (state in access-token only)
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		// Disable CSRF because of state-less session-management
		http.csrf().disable();

		// Return 401 (unauthorized) instead of 403 (redirect to login) when
		// authorization is missing or invalid
		http.exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
			response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Restricted Content\"");
			response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
		});

		// If SSL enabled, disable http (https only)
		if (serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled()) {
			http.requiresChannel().anyRequest().requiresSecure();
		} else {
			http.requiresChannel().anyRequest().requiresInsecure();
		}

		// Route security: authenticated to all routes but actuator and Swagger-UI
		// @formatter:off
        http.authorizeRequests()
            .requestMatchers(PUBLIC_URL).permitAll()
            .anyRequest().authenticated();
        // @formatter:on

		http.headers().xssProtection().and()
				.contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline'").and()
				.httpStrictTransportSecurity().requestMatcher(AnyRequestMatcher.INSTANCE);

		return http.build();
	}

}