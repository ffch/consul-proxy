package cn.pomit.consul.config;

import java.util.Set;

public interface Source {
	public Object setProperty(String key, String value);
	
	public String getProperty(String key);
	
	public Set<Object> keySet();
}
