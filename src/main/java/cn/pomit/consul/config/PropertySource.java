package cn.pomit.consul.config;

import java.util.Properties;
import java.util.Set;

public class PropertySource implements Source {
	Properties serverProperties;
	
	public PropertySource(Properties serverProperties) {
		this.serverProperties = serverProperties;
	}

	@Override
	public Object setProperty(String key, String value) {
		return serverProperties.setProperty(key, value);
	}

	@Override
	public String getProperty(String key) {
		return serverProperties.getProperty(key);
	}

	@Override
	public Set<Object> keySet() {
		return serverProperties.keySet();
	}

}
