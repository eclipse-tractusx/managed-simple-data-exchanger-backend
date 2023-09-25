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

package org.eclipse.tractusx.sde.core.utils;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TryUtils {
	
	private static Logger logger = LoggerFactory.getLogger(TryUtils.class);;

    /***
     * A Runnable which may throw an Exception
     *
     * @param <E>
     */
    @FunctionalInterface
    public interface ThrowableAction<E extends Exception> {
        void run() throws E;
    }

    /***
     * Tries to execute a Callable passed as a first parameter.
     *
     * @param callable - function to execute which may throw an Exception
     * @param onErr - code to be executed if an Exception happens
     * @return Optional with result of execution or empty if Exception was thrown
     * @param <V>
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public static <V> Optional<V> tryExec(Callable<V> callable, Consumer<Exception> onErr) {
        try {
            V v = callable.call();
            return Optional.of(v);
        } catch (Exception e) {
            onErr.accept(e);
            logger.error("context", e);
            return Optional.empty();
        }
    }

    private static <E extends Exception> Callable<Boolean> voidToBooleanAdapter(ThrowableAction<E> runnable) {
        return () -> {
            runnable.run();
            return true;
        };
    }

    /***
     * Try to execute a function given as a  first parameter {@link ThrowableAction}
     * If it is executed normally then returns true. On failure - false.
     *
     * @param runnable - a function to run which may throw an Exception
     * @param onErr - a Consumer to be executed if an Exception happens.Stacktrace is printed by default
     * @return true on success and false if an Exception has been thrown
     * @param <E>
     */
    public static <E extends Exception> boolean tryRun(ThrowableAction<E> runnable, Consumer<Exception> onErr) {
        return tryExec(voidToBooleanAdapter(runnable), onErr).orElse(false);
    }

    /***
     *
     * @return a function which ignores any given parameter
     * @param <ANY>
     */
    public static <ANY> Consumer<ANY> IGNORE() {
        return a -> {};
    }
}
