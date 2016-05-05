package io.zeymo.exec;

/**
 * Created By Zeymo at 15/6/4 15:54
 */
public class ExceptionUtil {

    public static Exception throwException(Throwable throwable){
        if(throwable instanceof Error){
            throw (Error)throwable;
        }else {
            return (Exception)throwable;
        }
    }
}
