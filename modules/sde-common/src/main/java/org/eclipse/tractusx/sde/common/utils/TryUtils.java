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

package org.eclipse.tractusx.sde.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;

public class TryUtils {
	
	private final static Logger logger = LoggerFactory.getLogger(TryUtils.class);

    /***
     * A Runnable which may throw an Exception
     *
     * @param <E>
     */
    @FunctionalInterface
    public interface ThrowableAction<E extends Exception> {
        void run() throws E;
    }

    @FunctionalInterface
    public interface ThrowableSupplier<R, E extends Exception> {
        R get() throws E;
    }

    /***
     * Tries to execute a Callable passed as a first parameter.
     *
     * @param throwableSupplier - function to execute which may throw an Exception
     * @param onErr - code to be executed if an Exception happens
     * @return Optional with result of execution or empty if Exception was thrown
     */
    @SuppressWarnings("unchecked")
    public static <V, E extends Exception> Optional<V>  tryExec(ThrowableSupplier<V, E> throwableSupplier, Consumer<E> onErr) {
        try {
            V v = throwableSupplier.get();
            return Optional.of(v);
        } catch (Exception e) {
            onErr.accept((E) e);
            logger.error("context", e);
            return Optional.empty();
        }
    }

    public static <E extends Exception> ThrowableSupplier<Boolean, E> voidToBooleanAdapter(ThrowableAction<E> runnable) {
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
     */
    public static <E extends Exception> boolean tryRun(ThrowableAction<E> runnable, Consumer<E> onErr) {
        return tryExec(voidToBooleanAdapter(runnable), onErr).orElse(false);
    }

    public static <ANY> void IGNORE(ANY any) {}

    public static <V, E extends Exception> V retryAdapter(ThrowableSupplier<V, E> callable, Runnable pause, int tries) throws E{
        Optional<V> res = Optional.empty();
        var errHandler = new Object() {
            E e;
            void setErr(E e) {
                this.e = e;
            }
        };
        for (int currentTry = 0; currentTry != tries && res.isEmpty(); currentTry++) {
            if (currentTry != 0) pause.run();
            res = tryExec(callable, errHandler::setErr);
        }
        return res.orElseThrow(() -> errHandler.e);
    }
}
