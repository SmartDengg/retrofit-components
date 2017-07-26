package smartdengg.retrofit2.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建时间: 2016/08/09 17:53 <br>
 * 作者: dengwei <br>
 * 描述: 用于标记该网络请求接口是否允许重试,如果被标记,则默认重试一次
 */
@Beat @Documented @Retention(value = RetentionPolicy.RUNTIME) @Target(value = ElementType.METHOD)
public @interface RetryCount {

  int count() default 1;
}
