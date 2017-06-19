package smartdengg.okhttp3.logging;

import android.util.Log;

import static smartdengg.okhttp3.logging.HttpLoggingInterceptor.LOG_TAG;

/**
 * 创建时间:  2017/03/07 15:22 <br>
 * 作者:  dengwei <br>
 * 描述:
 */
interface Logger {

  void log(String message);

  void logRequestBody(String message);

  void logTopBorder();

  void logMiddleBorder();

  void logBottomBorder();

  class BuiltInLogger implements Logger {

    private static String HTTP_TAG = LOG_TAG;
    private static final int MAX_LOG_LENGTH = 4 * 1000;

    private static final char TOP_LEFT_CORNER = '╔';
    private static final char TOP_LEFT_CORNER_SINGLE = '┌';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char BOTTOM_LEFT_CORNER_SINGLE = '└';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final char HORIZONTAL_DOUBLE_LINE_SINGLE = '│';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";

    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String TOP_BORDER_SINGE =
        TOP_LEFT_CORNER_SINGLE + SINGLE_DIVIDER + SINGLE_DIVIDER;
    private static final String BOTTOM_BORDER =
        BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER_SINGLE =
        BOTTOM_LEFT_CORNER_SINGLE + SINGLE_DIVIDER + SINGLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    @Override public void log(String message) {
      for (int i = 0, length = message.length(); i < length; i++) {
        int newline = message.indexOf('\n', i);
        newline = newline != -1 ? newline : length;
        do {
          int end = Math.min(newline, i + MAX_LOG_LENGTH);
          Log.d(HTTP_TAG, HORIZONTAL_DOUBLE_LINE + message.substring(i, end));
          i = end;
        } while (i < newline);
      }
    }

    @Override public void logRequestBody(String message) {
      this.log(" ");
      this.log("    " + TOP_BORDER_SINGE);
      this.log("    " + HORIZONTAL_DOUBLE_LINE_SINGLE + " " + message);
      this.log("    " + BOTTOM_BORDER_SINGLE);
      this.log(" ");
    }

    @Override public void logTopBorder() {
      Log.d(HTTP_TAG, TOP_BORDER);
    }

    @Override public void logMiddleBorder() {
      Log.d(HTTP_TAG, MIDDLE_BORDER);
    }

    @Override public void logBottomBorder() {
      Log.d(HTTP_TAG, BOTTOM_BORDER);
    }
  }
}
