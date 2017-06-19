package smartdengg.okhttp3.logging;

import com.smartdengg.androidjsonprinter.JsonPrinter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

import static smartdengg.okhttp3.logging.HttpLoggingInterceptor.LOG_TAG;
import static smartdengg.okhttp3.logging.Utils.UTF8;
import static smartdengg.okhttp3.logging.Utils.isPlaintext;

/**
 * 创建时间:  2017/03/07 15:55 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */

class ResponseDelegate {

  private Logger logger;
  private boolean logHeaders;
  private boolean logBody;

  static ResponseDelegate create(Logger logger, boolean logBody, boolean logHeaders) {
    return new ResponseDelegate(logger, logBody, logHeaders);
  }

  private ResponseDelegate(Logger logger, boolean logBody, boolean logHeaders) {
    this.logBody = logBody;
    this.logHeaders = logHeaders;
    this.logger = logger;
  }

  void printLog(Interceptor.Chain chain, Response response, Headers headers) throws IOException {

    long startMillis = response.sentRequestAtMillis();
    long endMillis = response.receivedResponseAtMillis();
    long tookMs = endMillis - startMillis;

    ResponseBody responseBody = response.body();
    long contentLength = responseBody.contentLength();
    String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
    logger.log("<-- " + response.code() + ' ' + response.message() + ' ' + response.request().
        url() + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')');

    if (logHeaders) {
      Buffer buffer = null;
      Charset charset = UTF8;

      for (int i = 0, count = headers.size(); i < count; i++) {
        logger.log(' ' + headers.name(i) + ": " + headers.value(i));
      }

      /*Outputs Middle_Border*/
      logger.logMiddleBorder();

      if (!logBody || !Utils.hasBody(response)) {
        logger.log("<-- END HTTP");
      } else if (Utils.bodyEncoded(headers)) {
        logger.log("<-- END HTTP (encoded body omitted)");
      } else {
        //Buffer buffer = new Buffer();
        //responseBody.source().readAll(buffer);
        BufferedSource responseSource = responseBody.source();
        responseSource.request(Long.MAX_VALUE); // Buffer the entire body.
        buffer = responseSource.buffer();
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
          try {
            charset = contentType.charset(UTF8);
          } catch (UnsupportedCharsetException e) {
            logger.log(" ");
            logger.log("Couldn't decode the response body; charset is likely malformed.");
            logger.log("<-- END HTTP");
            return;
          }
        }

        if (isPlaintext(buffer)) {
          logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
        } else {
          logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
        }
      }

      /*Outputs Bottom_Border*/
      logger.logBottomBorder();

      /*Outputs response as json format*/
      if (HttpLoggingInterceptor.DEBUG) {
        if (buffer == null || buffer.size() == 0) return;
        if (isPlaintext(buffer)) {
          JsonPrinter.d(LOG_TAG, buffer.clone().readString(charset),
              "URL: [ " + chain.request().url() + " ] ");
        }
      }
    }
  }
}
