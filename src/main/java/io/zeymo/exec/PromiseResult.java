package io.zeymo.exec;

/**
 * Created By Zeymo at 15/6/2 17:40
 */
public interface PromiseResult<T>{

    void success(T t);

    void error(Throwable throwable);

    default void accept(Result<T> result){
        if(result.isSuccess()){
            success(result.getValue());
        }else{
            error(result.getError());
        }
    }

}
