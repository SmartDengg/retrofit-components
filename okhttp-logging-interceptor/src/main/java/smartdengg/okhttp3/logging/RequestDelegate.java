package smartdengg.okhttp3.logging;

import java.io.IOException;
import java.nio.charset.Charset;
import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

import static smartdengg.okhttp3.logging.Utils.UTF8;
import static smartdengg.okhttp3.logging.Utils.bodyEncoded;
import static smartdengg.okhttp3.logging.Utils.isPlaintext;

/**
 * 创建时间:  2017/03/07 15:47 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */

class RequestDelegate {

  private Logger logger;
  private boolean logHeaders;
  private boolean logBody;

  static RequestDelegate create(Logger logger, boolean logBody, boolean logHeaders) {
    return new RequestDelegate(logger, logBody, logHeaders);
  }

  private RequestDelegate(Logger logger, boolean logBody, boolean logHeaders) {
    this.logger = logger;
    this.logBody = logBody;
    this.logHeaders = logHeaders;
  }

  void printLog(Interceptor.Chain chain, Request request, Headers headers) throws IOException {

    RequestBody requestBody = request.body();
    boolean hasRequestBody = requestBody != null;

    Connection connection = chain.connection();
    Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
    String requestStartMessage =
        "--> " + request.method() + ' ' + request.url() + ' ' + Utils.protocol(protocol);
    if (!logHeaders && hasRequestBody) {
      requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
    }

    /*Outputs Top_Border*/
    logger.logTopBorder();
    logger.log(requestStartMessage);

    if (logHeaders) {
      if (hasRequestBody) {
        if (requestBody.contentType() != null) {
          logger.log(" Content-Type: " + requestBody.contentType());
        }
        if (requestBody.contentLength() != -1) {
          logger.log(" Content-Length: " + requestBody.contentLength());
        }
      }

      for (int i = 0, count = headers.size(); i < count; i++) {
        String name = headers.name(i);
        // Skip headers from the request body as they are explicitly logged above.
        if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
          logger.log(' ' + name + ": " + headers.value(i));
        }
      }

      if (!logBody || !hasRequestBody) {
        logger.log("--> END " + request.method());
      } else if (bodyEncoded(headers)) {
        logger.log("--> END " + request.method() + " (encoded body omitted)");
      } else {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);

        Charset charset = UTF8;
        MediaType contentType = requestBody.contentType();
        if (contentType != null) charset = contentType.charset(UTF8);

        if (isPlaintext(buffer)) {
          logger.logRequestBody(buffer.readString(charset));
          logger.log(
              "--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
        } else {
          logger.log("--> END "
              + request.method()
              + " (binary "
              + requestBody.contentLength()
              + "-byte body omitted)");
        }
      }
    }

    /*Outputs Middle_Border*/
    logger.logMiddleBorder();
  }
}
