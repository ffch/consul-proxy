package cn.pomit.consul.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.handler.factory.ResourceHandlerFactory;
import cn.pomit.consul.http.HttpRequestMessage;
import cn.pomit.consul.http.HttpResponseMessage;

public abstract class AbstractResourceHandler {
	protected final Log log = LogFactory.getLog(getClass());
	protected ApplicationProperties applicationProperties;
	private static Map<String, Method> normalMethod = new HashMap<>();
	private static Map<String, Method> elMethod = new HashMap<>();

	protected static AbstractResourceHandler instance = null;

	public static void initInstance(Class<? extends AbstractResourceHandler> resourceHandlerClass,
			ApplicationProperties applicationProperties) throws Exception {
		if (resourceHandlerClass == null) {
			instance = new DefaultResourceHandler();
		} else {
			instance = resourceHandlerClass.newInstance();
		}
		instance.setApplicationProperties(applicationProperties);

		ResourceHandlerFactory.initMethodHandlers(resourceHandlerClass);
		ResourceHandlerFactory.initValues(instance, applicationProperties);
	}

	public static AbstractResourceHandler getInstance() {
		return instance;
	}

	public HttpResponseMessage handle(HttpRequestMessage httpRequestMessage) throws Exception {
		String url = httpRequestMessage.getUrl();
		log.debug("处理url：" + url);
		Method method = normalMethod.get(url);
		if (method == null) {
			method = elMethod.get(url);
		}
		if (method == null)
			return null;

		HttpResponseMessage res = null;

		res = (HttpResponseMessage) method.invoke(this, httpRequestMessage);
		return res;
	}

	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	public static Map<String, Method> getNormalMethod() {
		return normalMethod;
	}

	public static Map<String, Method> getElMethod() {
		return elMethod;
	}

}
