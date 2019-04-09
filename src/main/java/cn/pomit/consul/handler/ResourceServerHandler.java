package cn.pomit.consul.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.handler.factory.ResourceHandlerFactory;
import cn.pomit.consul.handler.method.HandlerMethod;
import cn.pomit.consul.handler.resource.AbstractResourceHandler;
import cn.pomit.consul.handler.resource.DefaultResourceHandler;
import cn.pomit.consul.http.HttpRequestMessage;
import cn.pomit.consul.http.HttpResponseMessage;

public class ResourceServerHandler {
	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected ApplicationProperties applicationProperties;
	private static Map<String, HandlerMethod> normalMethod = new HashMap<>();
	private static Map<String, HandlerMethod> elMethod = new HashMap<>();
	public List<AbstractResourceHandler> resourceHandlerList = new ArrayList<>();
	protected static ResourceServerHandler instance = null;

	public static void initInstance(List<Class<? extends AbstractResourceHandler>> resourceHandlerList,
			ApplicationProperties applicationProperties) throws Exception {
		instance = new ResourceServerHandler();

		if (resourceHandlerList == null) {
			instance.resourceHandlerList.add(new DefaultResourceHandler());
		} else {
			for (Class<? extends AbstractResourceHandler> item : resourceHandlerList) {
				instance.resourceHandlerList.add(item.newInstance());
			}
		}
		instance.setApplicationProperties(applicationProperties);

		for (AbstractResourceHandler resourceHandler : instance.resourceHandlerList) {
			resourceHandler.setApplicationProperties(applicationProperties);
			ResourceHandlerFactory.initMethodHandlers(resourceHandler);
			ResourceHandlerFactory.initValues(resourceHandler, applicationProperties);
		}

	}

	public static ResourceServerHandler getInstance() {
		return instance;
	}

	public HttpResponseMessage handle(HttpRequestMessage httpRequestMessage) throws Throwable {
		String url = httpRequestMessage.getUrl();
		log.trace("处理url：" + url);
		HandlerMethod handlerMethod = normalMethod.get(url);
		if (handlerMethod == null) {
			handlerMethod = elMethod.get(url);
		}
		if (handlerMethod == null)
			return null;

		HttpResponseMessage res = null;

		try {
			res = (HttpResponseMessage) handlerMethod.getMethod().invoke(handlerMethod.getResourceHandler(),
					httpRequestMessage);
		} catch (Exception e) {
			Throwable unwrap = unwrapThrowable(e);
			throw unwrap;
		}
		return res;
	}

	public static Throwable unwrapThrowable(Throwable wrapped) {
		Throwable unwrapped = wrapped;
		while (true) {
			if (unwrapped instanceof InvocationTargetException) {
				unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
			} else if (unwrapped instanceof UndeclaredThrowableException) {
				unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
			} else {
				return unwrapped;
			}
		}
	}

	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	public static Map<String, HandlerMethod> getNormalMethod() {
		return normalMethod;
	}

	public static Map<String, HandlerMethod> getElMethod() {
		return elMethod;
	}

}
