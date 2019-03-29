package cn.pomit.consul.handler.factory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.util.TypeUtils;

import cn.pomit.consul.annotation.Mapping;
import cn.pomit.consul.annotation.Value;
import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.handler.AbstractResourceHandler;
import cn.pomit.consul.util.ReflectUtil;
import io.netty.util.internal.StringUtil;

public class ResourceHandlerFactory {
	protected static Log log = LogFactory.getLog(ResourceHandlerFactory.class);
	public static AbstractResourceHandler createResourceHandler(
			Class<? extends AbstractResourceHandler> resourceHandlerClass, ApplicationProperties applicationProperties)
			throws Exception {
		AbstractResourceHandler resourceHandler = AbstractResourceHandler.getInstance();

		return resourceHandler;
	}

	public static void initValues(AbstractResourceHandler resourceHandler, ApplicationProperties applicationProperties)
			throws Exception {
		log.info("初始化@Value注解。。。");
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

	public static void initMethodHandlers(Class<? extends AbstractResourceHandler> resourceHandlerClass) {
		log.info("初始化@Mapping注解。。。");
		Method[] methods = resourceHandlerClass.getMethods();

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
				AbstractResourceHandler.getElMethod().put(param[0], methods[j]);
			} else {
				AbstractResourceHandler.getNormalMethod().put(methodValue, methods[j]);
			}

		}
	}
}
