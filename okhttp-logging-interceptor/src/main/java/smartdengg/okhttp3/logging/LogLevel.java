package smartdengg.okhttp3.logging;

/**
 * 创建时间:  2017/03/07 15:54 <br>
 * 作者:  dengwei <br>
 * 描述:
 */
public enum LogLevel {

  /** No logs. */
  NONE, /**
   * Logs request and response lines.
   * <p>Example:
   * <pre>{@code
   * --> POST /greeting HTTP/1.1 (3-byte body)
   * <-- HTTP/1.1 200 OK (22ms, 6-byte body)
   * }</pre>
   */
  BASIC, /**
   * Logs request and response lines and their respective headers.
   * <p>Example:
   * <pre>{@code
   * --> POST /greeting HTTP/1.1
   * Host: example.com
   * Content-Type: plain/text
   * Content-Length: 3
   * --> END POST
   * <-- HTTP/1.1 200 OK (22ms)
   * Content-Type: plain/text
   * Content-Length: 6
   * <-- END HTTP
   * }</pre>
   */
  HEADERS, /**
   * Logs request and response lines and their respective headers and bodies (if present).
   * <p>Example:
   * <pre>{@code
   * --> POST /greeting HTTP/1.1
   * Host: example.com
   * Content-Type: plain/text
   * Content-Length: 3
   * Hi?
   * --> END GET
   * <-- HTTP/1.1 200 OK (22ms)
   * Content-Type: plain/text
   * Content-Length: 6
   * Hello!
   * <-- END HTTP
   * }</pre>
   */
  BODY
}
