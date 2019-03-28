package cn.pomit.consul.endpoint;

import cn.pomit.consul.handler.ResourceHandler;
import cn.pomit.consul.handler.factory.ResourceHandlerFactory;

public class JsonHttpServer extends NettyServerTemplate {
	private Class<? extends ResourceHandler> resourceHandler = null;

	public JsonHttpServer(int port, String name, String charset) {
		this.port = port;
		this.name = name;
		this.charset = charset;
	}

	public JsonHttpServer(int port, String name) {
		this.port = port;
		this.name = name;
		this.charset = "UTF-8";
	}

	public JsonHttpServer(int port) {
		this.port = port;
	}

	public JsonHttpServer() {
	}

	public void setResourceHandler(Class<? extends ResourceHandler> resourceHandler) {
		this.resourceHandler = resourceHandler;
	}

	@Override
	protected ResourceHandler resourceHandler() throws Exception {
		return ResourceHandlerFactory.createResourceHandler(resourceHandler, consulProperties);
	}

}
