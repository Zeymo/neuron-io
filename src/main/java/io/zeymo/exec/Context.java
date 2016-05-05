package io.zeymo.exec;

import io.netty.channel.EventLoop;

/**
 * Created By Zeymo at 15/6/1 14:55
 */
public interface Context extends Runtime {

    static Context require(){
        return Runtime.acquire().getContext();
    }

    Runtime getRuntime();

    EventLoop getEventLoop();
}
