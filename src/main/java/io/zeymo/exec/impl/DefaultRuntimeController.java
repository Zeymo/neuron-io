package io.zeymo.exec.impl;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.zeymo.exec.Runtime;
import io.zeymo.exec.RuntimeController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created By Zeymo at 15/5/25 13:41
 */
public class DefaultRuntimeController implements RuntimeController {

    private final ExecutorService blockingExecutor;
    private final EventLoopGroup  eventLoopGroup;
    private final Runtime  runtime;
    private final int numOfThread;

    public DefaultRuntimeController(int numOfThread){
        this.numOfThread = numOfThread;
        this.blockingExecutor = Executors.newCachedThreadPool(new RuntimeThreadBindingFactory(false,"neuron-blocking", Thread.NORM_PRIORITY));
        this.eventLoopGroup = new NioEventLoopGroup(numOfThread,new RuntimeThreadBindingFactory(true,"neuron-compute",Thread.MAX_PRIORITY));
        this.runtime = new DefaultRuntime(this);
    }

    @Override
    public boolean isManagedThread() {
        return RuntimeController.acquire() == this;
    }

    @Override
    public Runtime getRuntime() {
        return runtime;
    }

    @Override
    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public ExecutorService getBlockingExecutor() {
        return blockingExecutor;
    }

    @Override
    public void shutdown() {
        eventLoopGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS);
        blockingExecutor.shutdown();
    }

    @Override
    public int getNumOfThread() {
        return numOfThread;
    }

    private class RuntimeThreadBindingFactory extends DefaultThreadFactory {

        private boolean compute;
        public RuntimeThreadBindingFactory(boolean compute,String poolName, int priority) {
            super(poolName, priority);
            this.compute = compute;
        }

        @Override
        public Thread newThread(Runnable r) {
            return super.newThread(() -> {
                RuntimeThreadBinding.set(DefaultRuntimeController.this);
                r.run();
            });
        }
    }
}
