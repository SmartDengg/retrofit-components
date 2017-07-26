package smartdengg.retrofit2.adapter.rxjava2;

import com.smartdengg.retrofit_adapter_rxjava2.R;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import java.lang.reflect.Type;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;

/**
 * 创建时间:  2017/02/22 16:01 <br>
 * 作者:  dengwei <br>
 * 描述:
 */
final class RxJava2Adapter implements CallAdapter<Object, Object> {

  private final Type responseType;
  private final int retryCount;
  private boolean isBody = false;
  private boolean isSingle = false;
  private boolean isMaybe = false;
  private boolean isCompletable = false;
  private final RxJava2CallAdapterFactory.Callback callback;

  RxJava2Adapter(Type responseType, int retryCount, boolean isBody, boolean isSingle,
      boolean isMaybe, boolean isCompletable, RxJava2CallAdapterFactory.Callback callback) {
    this.responseType = responseType;
    this.retryCount = retryCount;
    this.isBody = isBody;
    this.isSingle = isSingle;
    this.isMaybe = isMaybe;
    this.isCompletable = isCompletable;
    this.callback = callback;
  }

  @Override public Type responseType() {
    return responseType;
  }

  @SuppressWarnings("NullableProblems") @Override public Object adapt(final Call<Object> call) {

    Observable<Response<Object>> responseObservable =
        new CallExecuteObservable<>(call).map(new Function<Response<Object>, Response<Object>>() {
          @Override public Response<Object> apply(@NonNull Response<Object> response)
              throws Exception {
            if (callback != null) {
              try {
                callback.onResponse(response);
              } catch (Exception ex) {
                Exceptions.throwIfFatal(ex);
                RxJavaPlugins.onError(ex);
              }
            }
            return response;
          }
        }).retryWhen(new RetryWhenHandler(retryCount));
    Observable<?> observable;

    if (isBody) {
      observable = new BodyObservable<>(responseObservable);
    } else {
      observable = responseObservable;
    }

    if (isSingle) {
      return observable.singleOrError();
    } else if (isMaybe) {
      return observable.singleElement();
    } else if (isCompletable) {
      return observable.ignoreElements();
    }

    return observable;
  }
}
