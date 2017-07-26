package smartdengg.retrofit2.adapter.rxjava;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import smartdengg.retrofit2.utils.Utils;

/**
 * 创建时间:  2017/02/22 16:25 <br>
 * 作者:  dengwei <br>
 * 描述:
 */
final class RetryWhenHandler implements Func1<Observable<? extends Throwable>, Observable<Long>> {

  private static final int INITIAL = 1;
  private int maxConnectCount = 1;

  RetryWhenHandler(int retryCount) {
    this.maxConnectCount += retryCount;
  }

  @Override public Observable<Long> call(Observable<? extends Throwable> errorObservable) {
    return errorObservable.zipWith(Observable.range(INITIAL, maxConnectCount),
        new Func2<Throwable, Integer, ThrowableWrapper>() {
          @Override public ThrowableWrapper call(Throwable throwable, Integer i) {

            if (throwable instanceof IOException) return new ThrowableWrapper(throwable, i);
            return new ThrowableWrapper(throwable, maxConnectCount);
          }
        }).concatMap(new Func1<ThrowableWrapper, Observable<Long>>() {
      @Override public Observable<Long> call(ThrowableWrapper throwableWrapper) {

        final int retryCount = throwableWrapper.getRetryCount();

        if (maxConnectCount == retryCount) {
          return Observable.error(throwableWrapper.getSourceThrowable());
        }

        return Observable.timer((long) Math.pow(2, retryCount), TimeUnit.SECONDS,
            Schedulers.immediate());
      }
    });
  }

  private static final class ThrowableWrapper {

    private Throwable sourceThrowable;
    private int retryCount;

    ThrowableWrapper(Throwable sourceThrowable, Integer retryCount) {
      this.sourceThrowable = Utils.requireNonNull(sourceThrowable, "sourceThrowable == null");
      this.retryCount = Utils.requireNonNull(retryCount, "retryCount == null");
    }

    Throwable getSourceThrowable() {
      return sourceThrowable;
    }

    int getRetryCount() {
      return retryCount;
    }
  }
}
