package cn.pomit.consul.rest.client.function;

/**
 * Copy from jdk8 BiFunction
 *
 * @author wuguangkuo
 * @create 2018-12-12 17:20
 **/
public interface BiFunction<T, U, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    R apply(T t, U u);
}
