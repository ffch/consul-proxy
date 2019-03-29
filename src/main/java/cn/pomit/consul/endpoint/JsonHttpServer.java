package cn.pomit.consul.endpoint;

import cn.pomit.consul.handler.AbstractResourceHandler;
import cn.pomit.consul.handler.factory.ResourceHandlerFactory;

public class JsonHttpServer extends NettyServerTemplate {

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

	public JsonHttpServer(){
	}

	@Override
	protected AbstractResourceHandler resourceHandler() throws Exception {
		return ResourceHandlerFactory.createResourceHandler(resourceHandler, consulProperties);
	}

}
