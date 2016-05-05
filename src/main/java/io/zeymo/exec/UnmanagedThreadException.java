package io.zeymo.exec;

/**
 * Created By Zeymo at 15/5/25 11:32
 */
public class UnmanagedThreadException extends RuntimeException {

    public UnmanagedThreadException() {
        super("do operation in none neuron managed thread '"+Thread.currentThread()+"'");
    }
}
