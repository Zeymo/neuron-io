package io.zeymo.exec.impl;

import io.netty.channel.EventLoop;
import io.zeymo.exec.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 运行时逻辑上下文聚合根,一个逻辑上下文
 * Created By Zeymo at 15/5/25 11:30
 */
public class ContextController {

    private final EventLoop eventLoop;

    private final Action<? super Context> onComplete;

    private final BiAction<? super Context, Throwable> onError;

    private volatile boolean done;
    //逻辑顺序代码块，可能有另一个运行分支上下文的线程会操作主上下文所以需要同步
    private Queue<Deque<Fragment>> logicContext = new ConcurrentLinkedDeque<>();

    private DefaultContext context;

    public ContextController(RuntimeController controller, EventLoop eventLoop,
                             Action<? super Context> onComplete, BiAction<? super Context, Throwable> onError,
                             Action<Context> action) {
        this.eventLoop = eventLoop;
        this.onComplete = onComplete;
        this.onError = onError;

        context = new DefaultContext(controller, eventLoop);

        //执行用户的入口逻辑
        Deque<Fragment> userFragment = new ArrayDeque<>();
        userFragment.add(() -> action.execute(context));
        logicContext.add(userFragment);

        //上下文结束符号
        Deque<Fragment> completeFragment = new ArrayDeque<>(1);
        completeFragment.add(() -> done = true);
        logicContext.add(completeFragment);

        flush();
    }

    private void flush(){
        if(done) return;
        //当前已经有context已经在运行
        Optional<ContextController> optional =  ContextThreadBinding.get();
        ContextController current = optional.isPresent()?optional.get():null;
        if(this == current) return;

        //如果不是eventloop线程或者还有别的上下文在执行，在此上下文没有完成的前提下，需要重新在eventloop上排队
        if(!eventLoop.inEventLoop() || current != null){
            if(!done){
                eventLoop.execute(() -> flush());
            }
            return;
        }

        ContextThreadBinding.set(this);
        try{
            while(true){
                if(logicContext.isEmpty()) break;

                Fragment f = logicContext.element().poll();
                if(f == null){
                    logicContext.remove();
                    if(logicContext.isEmpty()){
                        if(done){
                            onComplete();
                            break;
                        }else{
                            //如果上下文没有结束说明分支上下文结束需要切换上下文
                            //在分支上下文（可能运行在另一个线程）调用complete方法来继续执行父上下文
                            break;
                        }
                    }
                }else{
                    try {
                        f.execute();
                    } catch (Throwable e) {
                        Deque<Fragment> events = logicContext.element();
                        events.clear();
                        events.add(() -> onError(context,e));
                    }
                }
            }
        }finally {
            ContextThreadBinding.remove();
        }

    }

    //创建分支上下文，保存主上下文的引用，并开始执行分支上下文
    public void createSubContext(Action<CombinedContext> action){
        if(done) return;

        if (logicContext.isEmpty()) {
            logicContext.add(new ArrayDeque<>());
        }

        logicContext.element().add(() -> {
            Queue<Deque<Fragment>> parent = logicContext;
            logicContext = new ConcurrentLinkedDeque<>();
            //放入空的deque是为了抛异常时不用new，其实谁new无所谓，这里new无非多一次循环
            logicContext.add(new ArrayDeque<>());
            CombinedContext cc = new CombinedContext(parent, logicContext);
            action.execute(cc);
        });
        flush();
    }

    private void onComplete(){
        try{
            onComplete.execute(context);
        }catch (Exception e){
            //TODO
        }
    }

    private void onError(Context context,Throwable t){
        try{
            onError.execute(context,t);
        }catch (Exception e){
            //TODO
        }
    }

    public static ContextController require() throws UnmanagedThreadException {
        return ContextThreadBinding.get().orElseThrow(UnmanagedThreadException::new);
    }

    public class CombinedContext{

        private Queue<Deque<Fragment>> parent;

        private Queue<Deque<Fragment>> current;

        private CombinedContext(Queue<Deque<Fragment>> parent, Queue<Deque<Fragment>> current) {
            this.parent = parent;
            this.current = current;
        }

        public void complete(Fragment f){
            System.out.println("complete in ["+Thread.currentThread()+"]");

            Deque<Fragment> cf = new ArrayDeque<>();
            cf.add(() -> {
                //切换到父上下文继续执行
                logicContext = parent;
                f.execute();
            });
            current.add(cf);
            flush();
        }
    }

    public DefaultContext getContext() {
        return context;
    }

    public EventLoop getEventLoop() {
        return eventLoop;
    }
}
