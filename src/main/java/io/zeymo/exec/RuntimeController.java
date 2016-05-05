package io.zeymo.exec;

import io.netty.channel.EventLoopGroup;
import io.zeymo.exec.impl.RuntimeThreadBinding;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Created By Zeymo at 15/5/25 11:36
 */
public interface RuntimeController {

    static Optional<RuntimeController> current(){
        return RuntimeThreadBinding.get();
    }

    static RuntimeController acquire() throws UnmanagedThreadException{
        return current().orElseThrow(UnmanagedThreadException::new);
    }

    boolean isManagedThread();

    Runtime getRuntime();

    EventLoopGroup getEventLoopGroup();

    ExecutorService getBlockingExecutor();

    void shutdown();

    int getNumOfThread();

}
