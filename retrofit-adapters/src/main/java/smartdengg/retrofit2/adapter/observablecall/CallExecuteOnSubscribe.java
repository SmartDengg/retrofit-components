package smartdengg.retrofit2.adapter.observablecall;

import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;

/**
 * 创建时间:  2017/02/22 16:17 <br>
 * 作者:  dengwei <br>
 * 描述:
 */
final class CallExecuteOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
  private final Call<T> originalCall;

  CallExecuteOnSubscribe(Call<T> originalCall) {
    this.originalCall = originalCall;
  }

  @Override public void call(Subscriber<? super Response<T>> subscriber) {
    // Since Call is a one-shot type, clone it for each new subscriber.
    Call<T> call = originalCall.clone();
    CallArbiter<T> arbiter = new CallArbiter<>(call, subscriber);
    subscriber.add(arbiter);
    subscriber.setProducer(arbiter);

    Response<T> response;
    try {
      response = call.execute();
    } catch (Throwable t) {
      Exceptions.throwIfFatal(t);
      arbiter.emitError(t);
      return;
    }
    arbiter.emitResponse(response);
  }
}
