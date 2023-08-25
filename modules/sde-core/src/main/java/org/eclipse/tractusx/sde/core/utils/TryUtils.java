package org.eclipse.tractusx.sde.core.utils;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class TryUtils {
    @FunctionalInterface
    public interface ThrowableAction<E extends Exception> {
        void run() throws E;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static <V> Optional<V> tryExec(Callable<V> callable, Consumer<Exception> onErr) {
        try {
            V v = callable.call();
            return Optional.of(v);
        } catch (Exception e) {
            onErr.accept(e);
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static <E extends Exception> Callable<Boolean> voidToBooleanAdapter(ThrowableAction<E> runnable) {
        return () -> {
            runnable.run();
            return true;
        };
    }

    public static <E extends Exception> boolean tryRun(ThrowableAction<E> runnable, Consumer<Exception> onErr) {
        return tryExec(voidToBooleanAdapter(runnable), onErr).orElse(false);
    }

    public static <ANY> Consumer<ANY> IGNORE() {
        return a -> {};
    }
}
