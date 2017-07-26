package smartdengg.retrofit2.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建时间: 2017/03/08 12:16 <br>
 * 作者: dengwei <br>
 * 描述: 标记处于测试阶段的类，函数或者注解，需要注意的是这些被标记的属性可能会成为正式版，也可能在将来的版本中删除
 */
@Documented @Retention(value = RetentionPolicy.SOURCE)
@Target(value = { ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE })
public @interface Beat {
}
