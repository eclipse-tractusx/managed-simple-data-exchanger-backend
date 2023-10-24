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

package org.eclipse.tractusx.sde.util;

import org.eclipse.tractusx.sde.common.utils.TryUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.junit.platform.commons.annotation.Testable;

@Testable
class UtilityFunctionsTest {

    @Test
    public void testRemoveLastSlashOfUrl() {
        assertEquals("https://example.org/example", UtilityFunctions.removeLastSlashOfUrl("https://example.org/example"));
        assertEquals("", UtilityFunctions.removeLastSlashOfUrl("/"));
    }

    @Test
    public void testRetry() throws Exception{
        var testFn = new Object() {
            int i = 0;

            int test(int inc) throws Exception {
                i += inc;
                if (i < 10) throw new Exception("Exception");
                return i;
            };
        };
        var res = TryUtils.retryAdapter(
                () -> testFn.test(3),
                () -> {},
                5
        );
        assertEquals(12, res);
    }

    @Test()
    public void testRetryThrow() throws Exception{
        var testFn = new Object() {
            int i = 0;

            int test(int inc) throws Exception {
                i += inc;
                if (i < 10) throw new Exception("Exception");
                return i;
            };
        };
        assertThrows( Exception.class, () -> TryUtils.retryAdapter(
                () -> testFn.test(1),
                () -> {},
                5
        ));
    }

}

