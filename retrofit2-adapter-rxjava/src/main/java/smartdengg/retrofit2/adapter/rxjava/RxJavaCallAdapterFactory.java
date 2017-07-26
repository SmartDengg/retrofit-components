package smartdengg.retrofit2.adapter.rxjava;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import rx.Completable;
import rx.Observable;
import rx.Single;
import smartdengg.retrofit2.annotation.RetryCount;

/**
 * 创建时间: 2016/08/09 18:11 <br>
 * 作者: dengwei <br>
 * 描述: retrofit体系中网络接口的定义采用责任链的设计模式,当网络请求接口的返回值被定义成{@link Observable},{@link Single}或
 * {@link Completable}时,该工厂类可创建一个真正执行网络请求的动态代理实例
 */
public final class RxJavaCallAdapterFactory extends CallAdapter.Factory {

  private Callback callback;

  /**
   * 创建工厂实例
   */
  public static RxJavaCallAdapterFactory create() {
    return new RxJavaCallAdapterFactory(null);
  }

  /**
   * 创建持有全局网络响应监听的工厂实例
   *
   * @param callback 网络监听回调函数
   */
  public static RxJavaCallAdapterFactory create(Callback callback) {
    return new RxJavaCallAdapterFactory(callback);
  }

  private RxJavaCallAdapterFactory(Callback callback) {
    this.callback = callback;
  }

  @SuppressWarnings("NullableProblems") @Override
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {

    final Class<?> rawType = getRawType(returnType);
    final boolean isObservable = rawType == Observable.class;
    final boolean isSingle = rawType == Single.class;
    final boolean isCompletable = rawType == Completable.class;
    if (!isObservable && !isSingle && !isCompletable) return null;

    int retryCount = 0;
    for (Annotation annotation : annotations) {
      if (!RetryCount.class.isAssignableFrom(annotation.getClass())) continue;
      retryCount = ((RetryCount) annotation).count();
      if (retryCount < 0) {
        throw new IllegalArgumentException("The value in '@RetryCount' must not be less than 0");
      }
    }

    if (isCompletable) {
      return new RxJavaAdapter(Void.class, retryCount, true, false, true, callback);
    }

    if (!(returnType instanceof ParameterizedType)) {
      String name = isSingle ? "Single" : "Observable";
      throw new IllegalStateException(name
          + " return type must be parameterized"
          + " as "
          + name
          + "<Foo> or "
          + name
          + "<? extends Foo>");
    }

    boolean isBody = false;
    Type responseType;
    final Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
    final Class<?> rawObservableType = getRawType(observableType);

    if (rawObservableType == Response.class) {
      if (!(observableType instanceof ParameterizedType)) {
        throw new IllegalStateException(
            "Response must be parameterized" + " as Response<Foo> or Response<? extends Foo>");
      }
      responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
    } else {
      responseType = observableType;
      isBody = true;
    }

    return new RxJavaAdapter(responseType, retryCount, isBody, isSingle, false, callback);
  }

  /**
   * 全局回调,用于所有返回值类型为'Observable','Single'或'Completable'时的响应结果的监听
   */
  public interface Callback {
    void onResponse(Response response);
  }
}
