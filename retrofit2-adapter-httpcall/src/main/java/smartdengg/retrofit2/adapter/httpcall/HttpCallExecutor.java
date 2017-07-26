package smartdengg.retrofit2.adapter.httpcall;

import java.io.IOException;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smartdengg.retrofit2.utils.Utils;

/**
 * 创建时间: 2016/08/09 18:06 <br>
 * 作者: dengwei <br>
 * 描述: 自定义HttpCallAdapter，用于返回值类型为HttpCall的接口执行真正的网络操作
 */
class HttpCallExecutor<T> implements HttpCall<T> {

  private static int CODE_204 = 204;
  private static int CODE_205 = 205;
  private static int CODE_400 = 400;
  private static int CODE_401 = 401;
  private static int CODE_500 = 500;
  private static int CODE_600 = 600;

  private Call<T> mDelegate;
  private MainThreadExecutor mMainThreadExecutor;
  private final int mMaxRetryCount;
  private int mCurrentRetryCount = 0;
  private HttpCallAdapterFactory.Callback globalCallback;

  HttpCallExecutor(Call<T> delegate, MainThreadExecutor mainThreadExecutor, int maxRetryCount,
      HttpCallAdapterFactory.Callback callback) {
    this.mDelegate = delegate;
    this.mMainThreadExecutor = mainThreadExecutor;
    this.mMaxRetryCount = maxRetryCount;
    this.globalCallback = callback;
  }

  @Override public Response<T> execute() throws IOException {
    return mDelegate.execute();
  }

  @Override public void enqueue(final HttpCallback<T> callback) {
    Utils.requireNonNull(callback, "callback == null");

    mDelegate.enqueue(new Callback<T>() {
      @Override public void onResponse(final Call<T> call, final Response<T> response) {

        mMainThreadExecutor.execute(new Runnable() {
          @Override public void run() {

            if (globalCallback != null) globalCallback.onResponse(response);

            callback.onResponse(response);

            int code = response.code();
            if (response.isSuccessful()) {
              if (code == CODE_204 || code == CODE_205 || response.body() == null) {
                callback.noContent(response, HttpCallExecutor.this);
              } else {
                callback.success(response.body(), HttpCallExecutor.this);
              }
            } else if (code == CODE_401) {
              callback.unauthenticated(response, HttpCallExecutor.this);
            } else if (code >= CODE_400 && code < CODE_500) {
              callback.clientError(response, HttpCallExecutor.this);
            } else if (code >= CODE_500 && code < CODE_600) {
              callback.serverError(response, HttpCallExecutor.this);
            } else {
              callback.unexpectedError(new RuntimeException("Unexpected response " + response),
                  HttpCallExecutor.this);
            }
          }
        });
      }

      @Override public void onFailure(Call<T> call, final Throwable t) {

        if (!isCanceled() && mCurrentRetryCount++ < mMaxRetryCount) { /*手动取消不做重试*/
          call.clone().enqueue(this);
        } else {
          mMainThreadExecutor.execute(new Runnable() {
            @Override public void run() {
              if (t instanceof IOException) {
                callback.networkError((IOException) t, HttpCallExecutor.this);
              } else {
                callback.unexpectedError(t, HttpCallExecutor.this);
              }
            }
          });
        }
      }
    });
  }

  @Override public void cancel() {
    mDelegate.cancel();
  }

  @Override public boolean isExecuted() {
    return mDelegate.isExecuted();
  }

  @Override public boolean isCanceled() {
    return mDelegate.isCanceled();
  }

  @SuppressWarnings("CloneDoesntCallSuperClone") @Override public HttpCall<T> clone() {
    return new HttpCallExecutor<>(mDelegate.clone(), mMainThreadExecutor, mMaxRetryCount,
        globalCallback);
  }

  @Override public Request request() {
    return mDelegate.request();
  }
}
