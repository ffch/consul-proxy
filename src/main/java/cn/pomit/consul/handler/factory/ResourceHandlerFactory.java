package cn.pomit.consul.handler.factory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.util.TypeUtils;

import cn.pomit.consul.annotation.Mapping;
import cn.pomit.consul.annotation.Value;
import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.handler.ResourceServerHandler;
import cn.pomit.consul.handler.method.HandlerMethod;
import cn.pomit.consul.handler.resource.AbstractResourceHandler;
import cn.pomit.consul.util.ReflectUtil;
import io.netty.util.internal.StringUtil;

public class ResourceHandlerFactory {
	protected static Logger log = LoggerFactory.getLogger(ResourceHandlerFactory.class);

	public static ResourceServerHandler createResourceServerHandler(ApplicationProperties applicationProperties)
			throws Exception {
		ResourceServerHandler resourceHandler = ResourceServerHandler.getInstance();

		return resourceHandler;
	}

	public static void initValues(AbstractResourceHandler resourceHandler, ApplicationProperties applicationProperties)
			throws Exception {
		log.info("初始化{}的@Value注解。。。", resourceHandler);
		Field fields[] = resourceHandler.getClass().getDeclaredFields();
		for (Field field : fields) {
			Value value = field.getAnnotation(Value.class);
			if (value == null)
				continue;
			
			Class<?> type = field.getType();
			String fieldName = field.getName();
			if (!ReflectUtil.isJavaClass(type))
				continue;

			Method setMethod = resourceHandler.getClass()
					.getMethod("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), type);
			String param = applicationProperties.getString(value.value());
			if (StringUtil.isNullOrEmpty(param))
				continue;

			setMethod.invoke(resourceHandler, TypeUtils.cast(param, type, null));
		}
	}

	public static void initMethodHandlers(AbstractResourceHandler resourceHandler) {
		log.info("初始化{}@Mapping注解。。。", resourceHandler);
		Method[] methods = resourceHandler.getClass().getMethods();

		for (int j = 0; j < methods.length; j++) {
			Mapping mapping = methods[j].getAnnotation(Mapping.class);
			if (mapping == null)
				continue;
			String methodValue = mapping.value();
			if ("".equals(methodValue)) {
				methodValue = "/" + methods[j].getName();
			} else if (!methodValue.startsWith("/")) {
				methodValue = "/" + methodValue;
			}
			if (methodValue.contains("**")) {
				String param[] = methodValue.split("\\*\\*");
				HandlerMethod handlerMethod = new HandlerMethod();
				handlerMethod.setMethod(methods[j]);
				handlerMethod.setResourceHandler(resourceHandler);
				ResourceServerHandler.getElMethod().put(param[0], handlerMethod);
			} else {
				HandlerMethod handlerMethod = new HandlerMethod();
				handlerMethod.setMethod(methods[j]);
				handlerMethod.setResourceHandler(resourceHandler);
				ResourceServerHandler.getNormalMethod().put(methodValue, handlerMethod);
			}

		}
	}
}
