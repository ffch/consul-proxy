package cn.pomit.consul.handler.method;

import java.lang.reflect.Method;

import cn.pomit.consul.handler.resource.AbstractResourceHandler;

public class HandlerMethod {
	private AbstractResourceHandler resourceHandler;
	private Method method;
	public AbstractResourceHandler getResourceHandler() {
		return resourceHandler;
	}
	public void setResourceHandler(AbstractResourceHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
}
