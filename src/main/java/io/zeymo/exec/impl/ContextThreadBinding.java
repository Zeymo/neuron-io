package io.zeymo.exec.impl;

import java.util.Optional;

/**
 * Created By Zeymo at 15/5/25 13:22
 */
public class ContextThreadBinding {

    private static final ThreadLocal<ContextController> THREAD_BINDING = new ThreadLocal<>();

    static void set(ContextController contextController) {
        THREAD_BINDING.set(contextController);
    }

    public static Optional<ContextController> get() {
        return Optional.ofNullable(THREAD_BINDING.get());
    }

    static void remove() {
        THREAD_BINDING.remove();
    }


}
