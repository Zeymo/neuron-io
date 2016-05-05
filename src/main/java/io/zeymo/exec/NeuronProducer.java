package io.zeymo.exec;

/**
 * Created By Zeymo at 15/5/25 13:14
 */
public interface NeuronProducer<T> {

    void emit(NeuronConsumer<T> out);
}
