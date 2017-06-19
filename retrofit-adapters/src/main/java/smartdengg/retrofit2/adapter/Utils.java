package smartdengg.retrofit2.adapter;

/**
 * 创建时间: 2016/08/09 15:14 <br>
 * 作者: dengwei <br>
 * 描述: 工具类
 */
public class Utils {

  private Utils() {
    throw new IllegalStateException("No instances!");
  }

  public static <T> T requireNonNull(T object, String message) {
    if (object == null) throw new NullPointerException(message);
    return object;
  }
}

