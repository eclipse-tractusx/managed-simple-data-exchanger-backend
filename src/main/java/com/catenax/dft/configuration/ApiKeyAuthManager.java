/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.catenax.dft.configuration;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;


@Slf4j
public class ApiKeyAuthManager implements AuthenticationManager {

    private final LoadingCache<String, Boolean> keys;

    public ApiKeyAuthManager(DataSource dataSource) {
        this.keys = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new DatabaseCacheLoader(dataSource));
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String principal = (String) authentication.getPrincipal();

        if (!keys.get(principal)) {
            throw new BadCredentialsException("The API key was not found or not the expected value.");
        } else {
            authentication.setAuthenticated(true);
            return authentication;
        }
    }

    private static class DatabaseCacheLoader implements CacheLoader<String, Boolean> {
        private final DataSource dataSource;

        DatabaseCacheLoader(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Boolean load(String key) {
            try (Connection conn = dataSource.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM auth WHERE api_key = ?")) {
                    ps.setObject(1, UUIDUtil.fromHex(key));

                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next();
                    }
                }
            } catch (Exception e) {
                log.error("An error occurred while retrieving api key from database", e);
                return false;
            }
        }
    }
}