package io.zeymo.exec.impl;

import io.zeymo.exec.*;

import java.util.function.Function;

/**
 * Created By Zeymo at 15/6/1 14:21
 */
public class DefaultPromise<T> implements Promise<T> {

    private final NeuronProducer<T> producer;

    public DefaultPromise(NeuronProducer<T> producer) {
        this.producer = producer;
    }

    @Override
    public void then(Action<? super T> then) {
        producer.emit(new NeuronConsumer<T>() {
            @Override
            public void success(T t) {
                try {
                    then.execute(t);
                } catch (Throwable e) {
                    throwError(e);
                }
            }

            @Override
            public void error(Throwable throwable) {
                throwError(throwable);
            }

            @Override
            public void complete() {}
        });
    }

    @Override
    public <O> Promise<O> transfer(Function<NeuronProducer<T>,NeuronProducer<O>> fn) {
        return new DefaultPromise<>(fn.apply(producer));
    }

    private void throwError(Throwable throwable){
        ContextController.require().createSubContext(combinedContext ->{
            combinedContext.complete(() -> {
                throw ExceptionUtil.throwException(throwable);
            });
        });
    }
}
