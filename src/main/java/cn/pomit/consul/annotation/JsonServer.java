package cn.pomit.consul.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.pomit.consul.handler.DefaultResourceHandler;
import cn.pomit.consul.handler.ResourceHandler;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonServer {
	int port() default -1;
	Class<? extends ResourceHandler> handler() default DefaultResourceHandler.class;
}