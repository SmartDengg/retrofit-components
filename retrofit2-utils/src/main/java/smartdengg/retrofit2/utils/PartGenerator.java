package smartdengg.retrofit2.utils;

import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 创建时间: 2016/08/09 17:08 <br>
 * 作者: dengwei <br>
 * 描述: 表单上传辅助类
 */

public class PartGenerator {

  private PartGenerator() {
    throw new IllegalStateException("No instance");
  }

  private static final MediaType MULTIPART_FORM_DATA = MultipartBody.FORM;

  /**
   * 提交表单请求时<b>MediaType</b>类型为'multipart/form-data'的{@link RequestBody}对象
   *
   * @param value 请求参数(key-value)中的值
   * @return multipart/form-data类型的RequestBoy
   */
  public static RequestBody createPartFromString(String value) {
    return RequestBody.create(MULTIPART_FORM_DATA, value);
  }

  /**
   * 提交表单请求时<b>MediaType</b>类型为'multipart/form-data'的{@link MultipartBody.Part}对象
   *
   * @param key 请求参数(key-value)中的键
   * @param file 请求参数(key-value)中的值
   * @return multipart/form-data类型的MultipartBody.Part
   */
  public static MultipartBody.Part createPartFromFile(String key, File file) {
    return createPartFromFile(MULTIPART_FORM_DATA, key, file);
  }

  /**
   * 提交表单请求时所需的{@link MultipartBody.Part}对象,需要传入正确的MediaType,如"image/*"或者"multipart/form-data"
   *
   * @param mediaType MediaType类型
   * @param key 请求参数(key-value)中的键
   * @param file 请求参数(key-value)中的值
   * @return 自定义MediaType类型的MultipartBody.Part
   */
  public static MultipartBody.Part createPartFromFile(MediaType mediaType, String key, File file) {

    // create RequestBody instance from file
    RequestBody requestFile =
        RequestBody.create(mediaType, Utils.requireNonNull(file, "file == null"));

    // MultipartBody.Part is used to send also the actual file name
    return MultipartBody.Part.createFormData(key, file.getName(), requestFile);
  }
}
