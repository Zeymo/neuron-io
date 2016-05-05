package io.zeymo.exec.impl;


import io.zeymo.exec.RuntimeController;

import java.util.Optional;

/**
 * Created By Zeymo at 15/5/25 13:22
 */
public class RuntimeThreadBinding {

    private static final ThreadLocal<RuntimeController> THREAD_BINDING = new ThreadLocal<>();

    static void set(RuntimeController runtimeController) {
        THREAD_BINDING.set(runtimeController);
    }

    public static Optional<RuntimeController> get() {
        return Optional.ofNullable(THREAD_BINDING.get());
    }
}
