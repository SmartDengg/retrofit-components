package smartdengg.retrofit2.adapter.rxjava;

import retrofit2.HttpException;
import retrofit2.Response;
import rx.Observable.Operator;
import rx.Producer;
import rx.Subscriber;
import rx.plugins.RxJavaHooks;

final class OperatorMapResponseToBodyOrError<T> implements Operator<T, Response<T>> {

  private static final OperatorMapResponseToBodyOrError<Object> INSTANCE =
      new OperatorMapResponseToBodyOrError<>();

  @SuppressWarnings("unchecked") static <R> OperatorMapResponseToBodyOrError<R> instance() {
    return (OperatorMapResponseToBodyOrError<R>) INSTANCE;
  }

  @Override public Subscriber<? super Response<T>> call(final Subscriber<? super T> child) {
    MapResponseSubscriber<T> parent = new MapResponseSubscriber<>(child);
    child.add(parent);
    return parent;
  }

  private static final class MapResponseSubscriber<T> extends Subscriber<Response<T>> {

    private Subscriber<? super T> actual;
    private boolean isTerminated;

    MapResponseSubscriber(Subscriber<? super T> actual) {
      this.actual = actual;
    }

    @Override public void onCompleted() {

      if (isTerminated) return;
      isTerminated = true;
      actual.onCompleted();
    }

    @Override public void onError(Throwable e) {

      if (isTerminated) {
        RxJavaHooks.onError(e);
        return;
      }
      isTerminated = true;
      actual.onError(e);
    }

    @Override public void onNext(Response<T> response) {

      if (actual.isUnsubscribed() || isUnsubscribed()) return;

      T body = response.body();
      if (response.isSuccessful() && body != null) {
        actual.onNext(body);
      } else {
        actual.onError(new HttpException(response));
      }
    }

    @Override public void setProducer(Producer p) {
      actual.setProducer(p);
    }
  }
}
