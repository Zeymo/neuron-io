package io.zeymo.exec.impl;


import io.zeymo.exec.Result;

/**
 * Created By Zeymo at 15/6/2 17:57
 */
public class DefaultResult<T> implements Result<T> {

    T value;

    Throwable throwable;

    public DefaultResult(T value) {
        this.value = value;
        this.throwable = null;
    }

    public DefaultResult(Throwable throwable) {
        this.value = null;
        this.throwable = throwable;
    }

    @Override
    public boolean isSuccess() {
        return throwable == null;
    }

    @Override
    public boolean isError() {
        return throwable != null;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public Throwable getError() {
        return throwable;
    }
}
