package smartdengg.retrofit2.adapter.httpcall;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import smartdengg.retrofit2.utils.Types;
import smartdengg.retrofit2.annotation.RetryCount;

/**
 * 创建时间: 2016/08/09 18:11 <br>
 * 作者: dengwei <br>
 * 描述: retrofit体系中网络接口的定义采用责任链的设计模式,当网络请求接口的返回值被定义成{@link HttpCall}时,
 * 该工厂类可创建一个真正执行网络请求的动态代理实例
 */
public class HttpCallAdapterFactory extends CallAdapter.Factory {

  private MainThreadExecutor mainThreadExecutor;
  private Callback callback;

  private HttpCallAdapterFactory(Callback callback) {
    this.mainThreadExecutor = new MainThreadExecutor();
    this.callback = callback;
  }

  /**
   * 创建工厂实例
   */
  public static HttpCallAdapterFactory create() {
    return new HttpCallAdapterFactory(null);
  }

  /**
   * 创建持有全局网络响应监听的工厂实例
   *
   * @param callback 网络监听回调函数
   */
  public static HttpCallAdapterFactory create(Callback callback) {
    return new HttpCallAdapterFactory(callback);
  }

  @Override
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {

    /*使用HttpCallAdapterFactory,函数的返回参数类型必须是HttpCall*/
    if (Types.getRawType(returnType) != HttpCall.class) return null;

    /*返回结果应该指定一个泛型，最起码也需要一个ResponseBody作为泛型*/
    if (!(returnType instanceof ParameterizedType)) {
      throw new IllegalStateException(
          "HttpCall must have generic type (e.g., HttpCall<ResponseBody>)");
    }

    final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
    final Class<?> rawResponseType = getRawType(responseType);
    if (rawResponseType == Response.class && responseType instanceof ParameterizedType) {
      throw new IllegalStateException(
          "我们现在还不支持将泛型参数化的Response<>(例如'HttpCall<Response<Foo>>')作为数据类型, "
              + "如果你非要这样，你可以使用将Observable<Response<Foo>>作为返回类型");
    }

    int retryCount = 0;
    for (Annotation annotation : annotations) {
      if (!RetryCount.class.isAssignableFrom(annotation.getClass())) continue;
      retryCount = ((RetryCount) annotation).count();
      if (retryCount < 0) {
        throw new IllegalArgumentException(
            "The value which in '@RetryCount' can not be less than 0");
      }
    }

    return new HttpCallAdapter(responseType, mainThreadExecutor, retryCount, callback);
  }

  /**
   * 全局回调,用于所有返回值类型为'HttpCall'时的响应结果的监听
   */
  @SuppressWarnings("WeakerAccess") public interface Callback {

    void onResponse(Response<?> response);
  }
}
