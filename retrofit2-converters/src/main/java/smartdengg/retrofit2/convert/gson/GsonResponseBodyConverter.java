package smartdengg.retrofit2.convert.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import retrofit2.Converter;

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

  private static final Charset UTF8 = Charset.forName("UTF-8");
  private Gson gson;
  private final TypeAdapter<T> adapter;

  GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
    this.gson = gson;
    this.adapter = adapter;
  }

  @Override public T convert(ResponseBody body) throws IOException {
    try {
      return getBody(body.charStream());
    } finally {
      Util.closeQuietly(body);
    }
  }

  private T getBody(Reader reader) throws IOException {
    JsonReader jsonReader = gson.newJsonReader(reader);
    jsonReader.setLenient(true);
    return adapter.read(jsonReader);
  }
}
