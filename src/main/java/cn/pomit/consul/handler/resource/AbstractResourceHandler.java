package cn.pomit.consul.handler.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.pomit.consul.config.ApplicationProperties;

public abstract class AbstractResourceHandler {
	protected final Log log = LogFactory.getLog(getClass());
	protected ApplicationProperties applicationProperties;

	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

}
