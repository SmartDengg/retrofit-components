package smartdengg.retrofit2.adapter.httpcall;

import java.io.IOException;
import retrofit2.Response;

/**
 * 创建时间: 2016/08/09 18:33 <br>
 * 作者: dengwei <br>
 * 描述: {@link HttpCall}中异步网络请求的回调接口
 */
public interface HttpCallback<T> {

  /** Called for received HTTP response. */
  void onResponse(Response<T> response);

  /** Called for [200, 300) responses. But not include 204 or 205 */
  void success(T entity, HttpCall<T> httpCall);

  /** Called for 204 and 205 */
  void noContent(Response<?> response, HttpCall<T> httpCall);

  /** Called for 401 responses. */
  void unauthenticated(Response<?> response, HttpCall<T> httpCall);

  /** Called for [400, 500) responses, except 401. */
  void clientError(Response<?> response, HttpCall<T> httpCall);

  /** Called for [500, 600) response. */
  void serverError(Response<?> response, HttpCall<T> httpCall);

  /** Called for network errors while making the call. */
  void networkError(IOException e, HttpCall<T> httpCall);

  /** Called for unexpected errors while making the call. */
  void unexpectedError(Throwable t, HttpCall<T> httpCall);
}
