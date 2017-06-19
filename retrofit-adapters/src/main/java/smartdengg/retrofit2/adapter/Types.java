package smartdengg.retrofit2.adapter;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * 创建时间: 16/7/19 下午3:02 <br>
 * 作者: SmartDengg <br>
 * 描述: 根据原始数据类型,推断class类型
 */
public class Types {

  public static Class<?> getRawType(Type type) {

    Utils.requireNonNull(type, "type == null");

    if (type instanceof Class<?>) {/*Type是一般类,如Foo*/
      // Type is a normal class.
      return (Class<?>) type;
    }
    if (type instanceof ParameterizedType) {/*Type是参数化泛型,如Foo<A>*/
      ParameterizedType parameterizedType = (ParameterizedType) type;
      // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
      // suspects some pathological case related to nested classes exists.
      Type rawType = parameterizedType.getRawType();
      if (!(rawType instanceof Class)) throw new IllegalArgumentException();
      return (Class<?>) rawType;
    }
    if (type instanceof GenericArrayType) {/*Type属于泛型数组,如Foo<String>[],T[]*/
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      return Array.newInstance(getRawType(componentType), 0).getClass();
    }
    /*Type属于泛型变量,如class Foo<T extends Number>,List<? extends T>,T[]中的"T",
    需要注意的是只有"Class","Method","Constructor","Executable"可以定义变量泛型*/
    if (type instanceof TypeVariable) {
      // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
      // type that's more general than necessary is okay.
      return Object.class;
    }
    if (type instanceof WildcardType) {/*Type属于通配符泛型,如"<?>"或者"<? extend A & B>"中的"?"*/
      return getRawType(((WildcardType) type).getUpperBounds()[0]);
    }

    throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
        + "GenericArrayType, but <"
        + type
        + "> is of type "
        + type.getClass().getName());
  }
}
