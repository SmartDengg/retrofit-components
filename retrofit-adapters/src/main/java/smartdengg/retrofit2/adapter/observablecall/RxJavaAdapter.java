package smartdengg.retrofit2.adapter.observablecall;

import java.lang.reflect.Type;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.plugins.RxJavaHooks;

/**
 * 创建时间:  2017/02/22 16:01 <br>
 * 作者:  dengwei <br>
 * 描述:
 */
final class RxJavaAdapter implements CallAdapter<Object, Object> {

  private final Type responseType;
  private final int retryCount;
  private boolean isBody = false;
  private boolean isSingle = false;
  private boolean isCompletable = false;
  private final RxJavaCallAdapterFactory.Callback callback;

  RxJavaAdapter(Type responseType, int retryCount, boolean isBody, boolean isSingle,
      boolean isCompletable, RxJavaCallAdapterFactory.Callback callback) {
    this.responseType = responseType;
    this.retryCount = retryCount;
    this.isBody = isBody;
    this.isSingle = isSingle;
    this.isCompletable = isCompletable;
    this.callback = callback;
  }

  @Override public Type responseType() {
    return responseType;
  }

  @Override public Object adapt(Call<Object> call) {

    Observable observable = Observable.create(new CallExecuteOnSubscribe<>(call))
        .map(new Func1<Response<?>, Response<?>>() {
          @Override public Response<?> call(Response<?> response) {
            if (callback != null) {
              try {
                callback.onResponse(response);
              } catch (RuntimeException e) {
                Exceptions.throwIfFatal(e);
                RxJavaHooks.onError(e);
              }
            }
            return response;
          }
        })
        .retryWhen(new RetryWhenHandler(retryCount));

    if (isBody) {
      //noinspection unchecked
      observable = observable.lift(OperatorMapResponseToBodyOrError.instance());
    }

    if (isSingle) {
      return observable.toSingle();
    } else if (isCompletable) {
      return observable.toCompletable();
    }

    return observable;
  }
}
