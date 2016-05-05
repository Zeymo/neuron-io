package io.zeymo.exec;

/**
 * Created By Zeymo at 15/5/25 13:14
 */
public interface NeuronConsumer<T> {

    void success(T t);

    void error(Throwable throwable);

    void complete();

    default void accept(Result<T> result){
        if(result.isSuccess()){
            success(result.getValue());
        }else{
            error(result.getError());
        }
    }

    default void accept(NeuronResult<T> result){
        if(result.isComplete()){
            complete();
        }else if(result.isSuccess()){
            success(result.getValue());
        }else{
            error(result.getError());
        }
    }
}
