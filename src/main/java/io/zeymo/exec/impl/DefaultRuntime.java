package io.zeymo.exec.impl;

import io.netty.channel.EventLoop;
import io.zeymo.exec.*;
import io.zeymo.exec.Runtime;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Created By Zeymo at 15/5/25 13:34
 */
public class DefaultRuntime implements Runtime {

    private final RuntimeController controller;

    public DefaultRuntime(RuntimeController controller) {
        this.controller = controller;
    }

    @Override
    public RuntimeController getRuntimeController() {
        return controller;
    }

    @Override
    public DefaultContext getContext() throws UnmanagedThreadException {
        return ContextController.require().getContext();
    }

    @Override
    public RuntimeStream exec() {
        return new DefaultRuntimeStream();
    }

    @Override
    public <T> Promise<T> blocking(Callable<T> operation) {
        return new DefaultPromise<>(consumer -> {
            ContextController controller = ContextController.require();
            controller.createSubContext(combinedContext -> {
                //lambda表达式的变量只能是final就用内部类吧
                CompletableFuture.supplyAsync(new Supplier<Result<T>>() {
                    Result<T> result;
                    @Override
                    public Result<T> get() {
                        try{
                            result = Result.success(operation.call());
                        }catch (Throwable throwable){
                            result = Result.error(throwable);
                        }
                        return result;
                    }
                },DefaultRuntime.this.controller.getBlockingExecutor()
                ).thenAcceptAsync(result -> combinedContext.complete(() -> consumer.accept(result)),controller.getEventLoop());
            });
        });
    }

    @Override
    public <T> Promise<T> promise(Action<PromiseResult<T>> operation) {
        return new DefaultPromise<>(consumer ->{
            ContextController controller = ContextController.require();
            controller.createSubContext(combinedContext -> {
                AtomicBoolean done = new AtomicBoolean(false);
                try{
                    operation.execute(new PromiseResult<T>(){
                        @Override
                        public void success(T t) {
                            if(checkState()){
                                combinedContext.complete(() -> consumer.success(t));
                            }
                        }

                        @Override
                        public void error(Throwable throwable) {
                            if(checkState()){
                                combinedContext.complete(() -> consumer.error(throwable));
                            }
                        }

                        private boolean checkState(){
                            if(!done.compareAndSet(false,true)){
                                //TODO LOG
                                return false;
                            }
                            return true;
                        }
                    });
                }catch (Throwable throwable){
                    if(!done.compareAndSet(false,true)){
                        //TODO LOG
                    }else{
                        combinedContext.complete(() -> consumer.error(throwable));
                    }
                }
            });
        });
    }

    private class DefaultRuntimeStream implements RuntimeStream {
        private Action<? super Context>              onComplete = Action.noop();
        private BiAction<? super Context, Throwable> onError    = BiAction.log();
        private EventLoop                    eventLoop  = controller.getEventLoopGroup().next();

        @Override
        public DefaultRuntimeStream eventLoop(EventLoop eventLoop) {
            this.eventLoop = eventLoop;
            return this;
        }

        @Override
        public RuntimeStream onError(BiAction<Context, Throwable> onError) {
            this.onError = onError;
            return this;
        }

        @Override
        public RuntimeStream onComplete(Action<Context> onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        @Override
        public void start(Action<Context> action) {
            if(eventLoop.inEventLoop()){
                //从Scheduler提交过来，就在eventloop中执行就好了
                try{
                    new ContextController(controller,eventLoop,onComplete,onError,action);
                }catch (Throwable throwable){
                    ExceptionUtil.throwException(throwable);
                }
            }else{
                eventLoop.submit(() -> new ContextController(controller,eventLoop,onComplete,onError,action));
            }
        }
    }
}
