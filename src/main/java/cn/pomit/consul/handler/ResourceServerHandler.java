package cn.pomit.consul.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.handler.factory.ResourceHandlerFactory;
import cn.pomit.consul.handler.method.HandlerMethod;
import cn.pomit.consul.handler.resource.AbstractResourceHandler;
import cn.pomit.consul.handler.resource.DefaultResourceHandler;
import cn.pomit.consul.http.HttpRequestMessage;
import cn.pomit.consul.http.HttpResponseMessage;

public class ResourceServerHandler {
	protected final Log log = LogFactory.getLog(getClass());
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

	public HttpResponseMessage handle(HttpRequestMessage httpRequestMessage) throws Exception {
		String url = httpRequestMessage.getUrl();
		log.debug("处理url：" + url);
		HandlerMethod handlerMethod = normalMethod.get(url);
		if (handlerMethod == null) {
			handlerMethod = elMethod.get(url);
		}
		if (handlerMethod == null)
			return null;

		HttpResponseMessage res = null;

		res = (HttpResponseMessage) handlerMethod.getMethod().invoke(handlerMethod.getResourceHandler(),
				httpRequestMessage);
		return res;
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
