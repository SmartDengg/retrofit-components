package smartdengg.retrofit2.adapter.rxjava2;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import smartdengg.retrofit2.utils.Utils;

/**
 * 创建时间:  2017/02/22 16:25 <br>
 * 作者:  dengwei <br>
 * 描述:
 */
final class RetryWhenHandler
    implements Function<Observable<? extends Throwable>, Observable<Long>> {

  private static final int INITIAL = 1;
  private int maxConnectCount = 1;

  RetryWhenHandler(int retryCount) {
    this.maxConnectCount += retryCount;
  }

  @Override public Observable<Long> apply(@NonNull Observable<? extends Throwable> errorObservable)
      throws Exception {

    return errorObservable.zipWith(Observable.range(INITIAL, maxConnectCount),
        new BiFunction<Throwable, Integer, ThrowableWrapper>() {
          @Override public ThrowableWrapper apply(Throwable throwable, Integer i) {

            if (throwable instanceof IOException) return new ThrowableWrapper(throwable, i);
            return new ThrowableWrapper(throwable, maxConnectCount);
          }
        }).concatMap(new Function<ThrowableWrapper, Observable<Long>>() {
      @Override public Observable<Long> apply(ThrowableWrapper throwableWrapper) {

        final int retryCount = throwableWrapper.getRetryCount();

        if (maxConnectCount == retryCount) {
          return Observable.error(throwableWrapper.getSourceThrowable());
        }

        return Observable.timer((long) Math.pow(2, retryCount), TimeUnit.SECONDS);
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
