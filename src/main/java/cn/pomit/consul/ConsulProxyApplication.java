package cn.pomit.consul;

import cn.pomit.consul.annotation.EnableServer;
import cn.pomit.consul.endpoint.JsonHttpServer;

public class ConsulProxyApplication {

	public static void run(Class<?> app) {
		try {
			EnableServer jsonServer = app.getAnnotation(EnableServer.class);
			int port = jsonServer.port();
			JsonHttpServer defaultJsonServer = null;
			if (port > 0) {
				defaultJsonServer = new JsonHttpServer(port);
			} else {
				defaultJsonServer = new JsonHttpServer();
			}
			defaultJsonServer.setResourceHandlers(jsonServer.handler());
			defaultJsonServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
