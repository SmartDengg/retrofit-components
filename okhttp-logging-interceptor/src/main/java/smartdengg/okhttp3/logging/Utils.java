package smartdengg.okhttp3.logging;

import java.io.Closeable;
import java.io.EOFException;
import java.nio.charset.Charset;
import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Response;
import okio.Buffer;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static okhttp3.internal.http.StatusLine.HTTP_CONTINUE;

/**
 * 创建时间: 2016/08/09 15:14 <br>
 * 作者: dengwei <br>
 * 描述: 工具类
 */
public class Utils {

  static final Charset UTF8 = Charset.forName("UTF-8");

  private Utils() {
    throw new IllegalStateException("No instances!");
  }

  static <T> T requireNonNull(T object, String message) {
    if (object == null) throw new NullPointerException(message);
    return object;
  }

  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (RuntimeException rethrown) {
        throw rethrown;
      } catch (Exception ignored) {
      }
    }
  }

  //http://stackoverflow.com/questions/7110750/how-do-popular-source-control-systems-differentiate-binary-files-from-text-files
  //http://eli.thegreenplace.net/2011/10/19/perls-guess-if-file-is-text-or-binary-implemented-in-python
  public static boolean isPlaintext(Buffer buffer) throws EOFException {
    try {
      Buffer prefix = new Buffer();
      long byteCount = buffer.size() < 64 ? buffer.size() : 64;
      buffer.copyTo(prefix, 0, byteCount);
      for (int i = 0; i < 16; i++) {
        if (prefix.exhausted()) {
          break;
        }
        int codePoint = prefix.readUtf8CodePoint();
        if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
          return false;
        }
      }
      return true;
    } catch (EOFException e) {
      return false; // Truncated UTF-8 sequence.
    }
  }

  static boolean bodyEncoded(Headers headers) {
    String contentEncoding = headers.get("Content-Encoding");
    return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
  }

  static String protocol(Protocol protocol) {
    return protocol == Protocol.HTTP_1_0 ? "HTTP/1.0" : "HTTP/1.1";
  }

  static boolean hasBody(Response response) {
    // HEAD requests never yield a body regardless of the response headers.
    if (response.request().method().equals("HEAD")) return false;

    int responseCode = response.code();
    if ((responseCode < HTTP_CONTINUE || responseCode >= 200)
        && responseCode != HTTP_NO_CONTENT
        && responseCode != HTTP_NOT_MODIFIED) {
      return true;
    }

    // If the Content-Length or Transfer-Encoding headers disagree with the
    // response code, the response is malformed. For best compatibility, we
    // honor the headers.
    return Long.parseLong(response.header("Content-Length")) != -1 || "chunked".equalsIgnoreCase(
        response.header("Transfer-Encoding"));
  }
}

