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