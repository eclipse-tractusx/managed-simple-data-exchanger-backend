/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
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

package com.catenax.dft.configuration;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import lombok.SneakyThrows;

public final class UUIDUtil {

    private UUIDUtil() {
    }

    @SneakyThrows
    public static UUID fromHex(String uuid) {
        byte[] data = Hex.decodeHex(uuid.toCharArray());
        return new UUID(ByteBuffer.wrap(data, 0, 8).getLong(), ByteBuffer.wrap(data, 8, 8).getLong());
    }
}