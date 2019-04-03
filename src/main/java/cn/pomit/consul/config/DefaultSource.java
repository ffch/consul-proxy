package cn.pomit.consul.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultSource implements Source {
	private Map<Object, Object> params = new HashMap<>();

	@Override
	public Object setProperty(String key, String value) {
		return params.put(key, value);
	}

	@Override
	public String getProperty(String key) {
		Object oval = params.get(key);
		if(oval == null)return null;
        String sval = (oval instanceof String) ? (String)oval : null;
		return sval;
	}

	@Override
	public Set<Object> keySet() {
		return params.keySet();
	}
	
	
}
