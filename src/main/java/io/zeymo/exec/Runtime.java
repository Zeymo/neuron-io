package io.zeymo.exec;

import io.zeymo.exec.impl.DefaultContext;

import java.util.concurrent.Callable;

/**
 * neuron线程的runtime实例
 * Created By Zeymo at 15/5/25 11:30
 */
public interface Runtime {

    /**
     * 获取当前线程绑定的runtime，如果不存在直接抛异常
     * @return
     * @throws UnmanagedThreadException
     */
    static Runtime acquire() throws UnmanagedThreadException{
        return RuntimeController.acquire().getRuntime();
    }

    /**
     * 获取当前线程的runtime控制器
     * @return RuntimeLifeCycle
     */
    RuntimeController getRuntimeController();

    /**
     * 获取当前线程的context控制器
     * @return RuntimeLifeCycle
     */
    DefaultContext getContext() throws UnmanagedThreadException ;

    /**
     * 获取runtime stream 实例
     * @return
     */
    RuntimeStream exec();

    /**
     * "同步"调用，立即返回一个Promise
     * operation会在blocking的线程池中执行并会在完成之后重新回来eventloop线程执行上下文
     * 此方法中不要做过多的计算操作，让操作尽快返回
     * 计算操作请用链式调用到主线程(i.e. eventloop)中操作
     * @param operation
     * @param <T>
     * @return
     */
    <T> Promise<T> blocking(Callable<T> operation);

    /**
     * 异步调用，返回值会给Consumer处理
     * @param operation
     * @param <T>
     * @return
     */
    <T> Promise<T> promise(Action<PromiseResult<T>> operation);

    /**
     * 生成给定值的promise
     * @param val
     * @param <T>
     * @return
     */
    default <T> Promise<T> of(T val) {
        return promise(result -> result.success(val));
    }


}
