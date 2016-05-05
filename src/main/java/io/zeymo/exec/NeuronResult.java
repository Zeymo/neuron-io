package io.zeymo.exec;

/**
 * Created By Zeymo at 15/6/4 13:20
 */
public interface NeuronResult<T> extends Result<T>{

    boolean isComplete();
}
