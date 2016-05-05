package io.zeymo.exec;

/**
 * Created By Zeymo at 15/5/25 13:10
 */
@FunctionalInterface
public interface BiAction<F,S> {

    void execute(F fist, S second) throws Exception;

    static BiAction<Context,Throwable> log(){
        return (context,throwable) -> {
            //TODO log};
        };
    }
}
