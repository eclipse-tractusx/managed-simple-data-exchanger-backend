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

package org.eclipse.tractusx.sde.portal.utils;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class KeycloakUtil {

    @Value("${keycloak.auth-server-url}")
    private String authUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${clientSecret}")
    private String clientSecret;
    @Value("${clientId}")
    private String clientId;

    public String getKeycloakToken() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authUrl)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build())
                .build();
        AccessTokenResponse accessTokenResponse = getAccessTokenResponse(keycloak);
        return (accessTokenResponse == null ? null : accessTokenResponse.getToken());

    }

    private AccessTokenResponse getAccessTokenResponse(Keycloak keycloak) {
        try {
            return keycloak.tokenManager().getAccessToken();
        } catch (Exception ex) {
            return null;
        }
    }
}
