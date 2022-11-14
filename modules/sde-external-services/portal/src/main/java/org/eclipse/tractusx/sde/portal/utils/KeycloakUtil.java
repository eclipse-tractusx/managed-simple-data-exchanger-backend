package org.eclipse.tractusx.sde.portal.utils;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
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
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
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
