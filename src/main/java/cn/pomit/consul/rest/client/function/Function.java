package cn.pomit.consul.rest.client.function;

/**
 * copy from jdk8 Function
 *
 * @author wuguangkuo
 * @create 2018-12-12 16:58
 **/
public interface Function<T, R> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t);
}
