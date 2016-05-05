package io.zeymo.exec.impl;

import io.netty.channel.EventLoop;
import io.zeymo.exec.*;
import io.zeymo.exec.Runtime;

import java.util.concurrent.Callable;

/**
 * Created By Zeymo at 15/6/1 16:54
 */
public class DefaultContext implements Context {

    private final RuntimeController runtimeController;

    private final EventLoop eventLoop;

    public DefaultContext(RuntimeController controller, EventLoop eventLoop) {
        this.runtimeController = controller;
        this.eventLoop = eventLoop;
    }

    @Override
    public RuntimeController getRuntimeController() {
        return runtimeController;
    }

    @Override
    public DefaultContext getContext() throws UnmanagedThreadException {
        return ContextController.require().getContext();
    }

    @Override
    public RuntimeStream exec() {
        return getRuntime().exec();
    }

    @Override
    public <T> Promise<T> blocking(Callable<T> operation) {
        return getRuntime().blocking(operation);
    }

    @Override
    public <T> Promise<T> promise(Action<PromiseResult<T>> operation) {
        return getRuntime().promise(operation);
    }

    @Override
    public Runtime getRuntime() {
        return runtimeController.getRuntime();
    }

    @Override
    public EventLoop getEventLoop() {
        return eventLoop;
    }
}
