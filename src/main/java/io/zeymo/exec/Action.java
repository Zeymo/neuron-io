package io.zeymo.exec;

/**
 * 和原生的{@link java.util.function.Consumer}相比就是会抛异常
 * Created By Zeymo at 15/5/25 13:10
 */
@FunctionalInterface
public interface Action<T>{

    void execute(T t) throws Exception;

    static Action<Object> noop(){
        return empty -> {};
    }
}
