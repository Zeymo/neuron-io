package io.zeymo.exec;

import io.netty.channel.EventLoop;

/**
 * 处理一个请求的入口，stream api模式
 * Created By Zeymo at 15/5/25 11:36
 */
public interface RuntimeStream {

    void start(Action<Context> action);

    RuntimeStream eventLoop(EventLoop eventLoop);

    RuntimeStream onError(BiAction<Context, Throwable> onError);

    RuntimeStream onComplete(Action<Context> onComplete);

}
