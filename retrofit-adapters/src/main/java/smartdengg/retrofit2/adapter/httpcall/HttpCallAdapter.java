package smartdengg.retrofit2.adapter.httpcall;

import java.lang.reflect.Type;
import retrofit2.Call;
import retrofit2.CallAdapter;
import smartdengg.retrofit2.adapter.MainThreadExecutor;

/**
 * 创建时间:  2017/02/22 17:20 <br>
 * 作者:  dengwei <br>
 * 描述:
 */
class HttpCallAdapter implements CallAdapter<Object, HttpCall<Object>> {

  private Type responseType;
  private MainThreadExecutor threadExecutor;
  private int retryCount;
  private HttpCallAdapterFactory.Callback callback;

  HttpCallAdapter(Type responseType, MainThreadExecutor threadExecutor,
      int retryCount, HttpCallAdapterFactory.Callback callback) {
    this.responseType = responseType;
    this.threadExecutor = threadExecutor;
    this.retryCount = retryCount;
    this.callback = callback;
  }

  @Override public Type responseType() {
    return responseType;
  }

  @Override public HttpCall<Object> adapt(Call<Object> call) {
    return new HttpCallExecutor<>(call, threadExecutor, retryCount, callback);
  }
}
