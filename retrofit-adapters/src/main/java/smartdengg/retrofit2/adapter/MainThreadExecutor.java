package smartdengg.retrofit2.adapter;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executor;

/**
 * 创建时间:  2017/04/05 17:42 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public class MainThreadExecutor implements Executor {

  private Handler mainHandler = new Handler(Looper.getMainLooper());

  @Override public void execute(@SuppressWarnings("NullableProblems") Runnable runnable) {
    mainHandler.post(requireNonNull(runnable, "runnable == null"));
  }

  private static <T> T requireNonNull(T object, String message) {
    if (object == null) throw new NullPointerException(message);
    return object;
  }
}
