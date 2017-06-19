package smartdengg.retrofit2.adapter.observablecall;

import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.Call;
import retrofit2.Response;
import rx.Producer;
import rx.Subscriber;
import rx.Subscription;
import rx.exceptions.CompositeException;
import rx.exceptions.Exceptions;
import rx.plugins.RxJavaHooks;
import rx.plugins.RxJavaPlugins;

/**
 * 创建时间:  2017/05/25 11:58 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
final class CallArbiter<T> extends AtomicInteger implements Subscription, Producer {
  private static final int STATE_WAITING = 0;
  private static final int STATE_REQUESTED = 1;
  private static final int STATE_HAS_RESPONSE = 2;
  private static final int STATE_TERMINATED = 3;

  private final Call<T> call;
  private final Subscriber<? super Response<T>> subscriber;

  private volatile Response<T> response;

  CallArbiter(Call<T> call, Subscriber<? super Response<T>> subscriber) {
    super(STATE_WAITING);

    this.call = call;
    this.subscriber = subscriber;
  }

  @Override public void unsubscribe() {
    call.cancel();
  }

  @Override public boolean isUnsubscribed() {
    return call.isCanceled();
  }

  @Override public void request(long amount) {
    if (amount == 0) return;

    while (true) {
      int state = get();
      switch (state) {
        case STATE_WAITING:
          if (compareAndSet(STATE_WAITING, STATE_REQUESTED)) return;
          break; // State transition failed. Try again.

        case STATE_HAS_RESPONSE:
          if (compareAndSet(STATE_HAS_RESPONSE, STATE_TERMINATED)) {
            deliverResponse(response);
            return;
          }
          break; // State transition failed. Try again.

        case STATE_REQUESTED:
        case STATE_TERMINATED:
          return; // Nothing to do.

        default:
          throw new IllegalStateException("Unknown state: " + state);
      }
    }
  }

  void emitResponse(Response<T> response) {
    while (true) {
      int state = get();
      switch (state) {
        case STATE_WAITING:
          this.response = response;
          if (compareAndSet(STATE_WAITING, STATE_HAS_RESPONSE)) return;
          break; // State transition failed. Try again.

        case STATE_REQUESTED:
          if (compareAndSet(STATE_REQUESTED, STATE_TERMINATED)) {
            deliverResponse(response);
            return;
          }
          break; // State transition failed. Try again.

        case STATE_HAS_RESPONSE:
        case STATE_TERMINATED:
          throw new AssertionError();

        default:
          throw new IllegalStateException("Unknown state: " + state);
      }
    }
  }

  private void deliverResponse(Response<T> response) {
    try {
      if (!isUnsubscribed()) subscriber.onNext(response);
    } catch (Throwable t) {
      Exceptions.throwIfFatal(t);
      try {
        subscriber.onError(t);
      } catch (Throwable inner) {
        Exceptions.throwIfFatal(inner);
        CompositeException composite = new CompositeException(t, inner);
        RxJavaHooks.onError(composite);
      }
      return;
    }
    try {
      subscriber.onCompleted();
    } catch (Throwable t) {
      Exceptions.throwIfFatal(t);
      RxJavaHooks.onError(t);
    }
  }

  void emitError(Throwable t) {
    set(STATE_TERMINATED);

    if (!isUnsubscribed()) {
      try {
        subscriber.onError(t);
      } catch (Throwable inner) {
        Exceptions.throwIfFatal(inner);
        CompositeException composite = new CompositeException(t, inner);
        RxJavaPlugins.getInstance().getErrorHandler().handleError(composite);
      }
    }
  }
}
