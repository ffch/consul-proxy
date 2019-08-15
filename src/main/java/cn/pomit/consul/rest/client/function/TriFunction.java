package cn.pomit.consul.rest.client.function;

/**
 * 带有三个参数的Function
 * @param <T> 第一个argument
 * @param <S> 第二个argument
 * @param <U> 第三个argument
 * @param <R> 返回类型
 * @author wuguangkuo
 **/
public interface TriFunction<T, S, U, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param s the second function argument
     * @param u the third function argument
     * @return the function result
     */
    R apply(T t, S s, U u);
}
