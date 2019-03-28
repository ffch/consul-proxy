package cn.pomit.consul.handler;

import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.http.HttpRequestMessage;
import cn.pomit.consul.http.HttpResponseMessage;

public abstract class ResourceHandler {
	protected ApplicationProperties applicationProperties;
	
	public abstract HttpResponseMessage handle(HttpRequestMessage httpRequestMessage);

	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
}
