package cn.pomit.consul.handler.factory;

import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.handler.DefaultResourceHandler;
import cn.pomit.consul.handler.ResourceHandler;

public class ResourceHandlerFactory {

	public static ResourceHandler createResourceHandler(Class<? extends ResourceHandler> resourceHandlerClass, ApplicationProperties applicationProperties) throws InstantiationException, IllegalAccessException{
		ResourceHandler resourceHandler = null;
		if(resourceHandlerClass == null){
			resourceHandler = new DefaultResourceHandler();
		}else{
			resourceHandler = resourceHandlerClass.newInstance();
		}
		resourceHandler.setApplicationProperties(applicationProperties);
		
		return resourceHandler;
	}
}
