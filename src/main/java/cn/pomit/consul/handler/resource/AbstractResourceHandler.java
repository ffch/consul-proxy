package cn.pomit.consul.handler.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.pomit.consul.config.ApplicationProperties;

public abstract class AbstractResourceHandler {
	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected ApplicationProperties applicationProperties;

	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

}
