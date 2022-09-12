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

package com.catenax.dft.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilityFunctionsTest {

    @Test
    void testRemoveLastSlashOfUrl() {
        assertEquals("https://example.org/example", UtilityFunctions.removeLastSlashOfUrl("https://example.org/example"));
        assertEquals("", UtilityFunctions.removeLastSlashOfUrl("/"));
    }

    @Test
    void testGetDurationValueForYears() {
        assertEquals("3 Year(s)", UtilityFunctions.getDurationValue("P3Y0M0DT0H0M0S"));
    }

    @Test
    void testGetDurationValueForMonths() {
        assertEquals("8 Month(s)", UtilityFunctions.getDurationValue("P0Y8M0DT0H0M0S"));
    }

    @Test
    void testGetDurationValueForDays() {
        assertEquals("7 Day(s)", UtilityFunctions.getDurationValue("P0Y0M7DT0H0M0S"));
    }

    @Test
    void testGetDurationValueForHours() {
        assertEquals("5 Hour(s)", UtilityFunctions.getDurationValue("P0Y0M0DT5H0M0S"));
    }

    @Test
    void testGetDurationValueForMinutes() {
        assertEquals("9 Minute(s)", UtilityFunctions.getDurationValue("P0Y0M0DT0H9M0S"));
    }

    @Test
    void testGetDurationValueForSeconds() {
        assertEquals("100 Second(s)", UtilityFunctions.getDurationValue("P0Y0M0DT0H0M100S"));
    }

    @Test
    void testGetDurationValueForNull() {
        assertEquals(null, UtilityFunctions.getDurationValue("P0Y0M0DT0H0M0S"));
    }
}

