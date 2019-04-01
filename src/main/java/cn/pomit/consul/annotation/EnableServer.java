package cn.pomit.consul.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.pomit.consul.handler.resource.AbstractResourceHandler;
import cn.pomit.consul.handler.resource.DefaultResourceHandler;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableServer {
	int port() default -1;
	Class<? extends AbstractResourceHandler>[] handler() default {DefaultResourceHandler.class};
}