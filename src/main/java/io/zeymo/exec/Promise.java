package io.zeymo.exec;

import java.util.function.Function;

/**
 * Created By Zeymo at 15/5/25 11:47
 */
public interface Promise<T> {

    /**
     * 触发observableStream向observerStream发送消息
     * @param then
     */
    void then(Action<? super T> then);

    /**
     * 将promise转换成另一个类型的对象，比如rx.Observable
     * @param fn
     * @return
     */
    default <O> O to(Function<Promise<T>, O> fn){
        return fn.apply(this);
    }

    /**
     * 错误回调
     * @return
     */
    default Promise<T> onError(Action<Throwable> action){
        return transfer( producer ->
            (subscriber) -> {
                producer.emit(new NeuronConsumer<T>() {
                    @Override
                    public void success(T t) {
                        subscriber.success(t);
                    }

                    @Override
                    public void error(Throwable throwable) {
                        try {
                            action.execute(throwable);
                        } catch (Exception e) {
                            subscriber.error(e);
                        }
                        subscriber.complete();
                    }
                    @Override
                    public void complete() {
                        subscriber.complete();
                    }
                });
            }
        );
    }

    /**
     *
     * @return
     */
    <O> Promise<O> transfer(Function<NeuronProducer<T>, NeuronProducer<O>> fn);

}
