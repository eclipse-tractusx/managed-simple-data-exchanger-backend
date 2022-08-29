package com.catenax.dft.configuration;

import javax.servlet.Filter;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@KeycloakConfiguration
@EnableGlobalMethodSecurity(jsr250Enabled  = true)
@Import(KeycloakSpringBootConfigResolver.class)
public class WebSecurityKeyclockConfig extends KeycloakWebSecurityConfigurerAdapter {


	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
	
		http.csrf().disable().authorizeRequests().antMatchers("/ping").permitAll();
		http.addFilterAfter(customAuthFilter(),BasicAuthenticationFilter.class).authorizeRequests().antMatchers("/public/*").authenticated();

	}
	@Bean
	public ApiHeaderAuthFilter customAuthFilter(){
	  return new ApiHeaderAuthFilter();
	}

	@Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
    	KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    public FilterRegistrationBean<Filter> keycloakAuthenticationProcessingFilterRegistrationBean(
            KeycloakAuthenticationProcessingFilter filter) {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(filter);

        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<Filter> keycloakPreAuthActionsFilterRegistrationBean(
            KeycloakPreAuthActionsFilter filter) {

        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<Filter> keycloakAuthenticatedActionsFilterBean(
            KeycloakAuthenticatedActionsFilter filter) {

        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(filter);

        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<Filter> keycloakSecurityContextRequestFilterBean(
            KeycloakSecurityContextRequestFilter filter) {

        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(filter);

        registrationBean.setEnabled(false);

        return registrationBean;
    }

    @Bean
    @Override
    @ConditionalOnMissingBean(HttpSessionManager.class)
    protected HttpSessionManager httpSessionManager() {
        return new HttpSessionManager();
    }
}
