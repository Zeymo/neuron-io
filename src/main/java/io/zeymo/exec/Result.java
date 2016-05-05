package io.zeymo.exec;

import io.zeymo.exec.impl.DefaultResult;

/**
 * Created By Zeymo at 15/6/2 17:40
 */
public interface Result<T> {

    static <T> Result success(T t){
        return new DefaultResult(t);
    }

    static Result error(Throwable throwable){
        return new DefaultResult(throwable);
    }

    boolean isSuccess();

    boolean isError();

    T getValue();

    Throwable getError();

}
