package cn.pomit.consul;

import cn.pomit.consul.annotation.JsonServer;
import cn.pomit.consul.endpoint.JsonHttpServer;

public class ConsulProxyApplication {

	public static void run(Class<?> app) {
		try {
			JsonServer jsonServer = app.getAnnotation(JsonServer.class);
			int port = jsonServer.port();
			JsonHttpServer defaultJsonServer = null;
			if (port > 0) {
				defaultJsonServer = new JsonHttpServer(port);
			} else {
				defaultJsonServer = new JsonHttpServer();
			}
			defaultJsonServer.setResourceHandler(jsonServer.handler());
			defaultJsonServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
