package cn.pomit.consul.config;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import cn.pomit.consul.endpoint.NettyServerTemplate;
import cn.pomit.consul.util.InetUtil;
import io.netty.util.internal.StringUtil;

public class ApplicationProperties {
	public static String DEFAULT_IP = "127.0.0.1";
	public static String DEFAULT_PORT = "8500";
	public static String DEFAULT_APP_PORT = "8080";
	public static String DEFAULT_SCHEME = "http";
	public static String DEFAULT_HEALTHCHECK_PATH = "/actuator/health";

	private String host;
	private int port = 8500;
	private String applicationName;
	private int applicationPort;

	private String ipAddress;

	private String hostname;

	private boolean preferIpAddress = false;

	private List<String> tags = new ArrayList<>();

	private String healthCheckPath = "/actuator/health";

	private String healthCheckUrl;

	private String healthCheckInterval = "10s";

	private String healthCheckTimeout;

	private String instanceId;

	private String scheme;

	private Properties serverProperties = new Properties();;

	public ApplicationProperties(Properties serverProperties, Integer serverPort) {
		this.serverProperties = serverProperties;
		host = serverProperties.getProperty("consul.host", DEFAULT_IP);
		port = Integer.parseInt(serverProperties.getProperty("consul.port", DEFAULT_PORT));
		if (serverPort == null) {
			applicationPort = Integer.parseInt(serverProperties.getProperty("application.port", DEFAULT_APP_PORT));
		} else {
			applicationPort = serverPort;
		}
		applicationName = serverProperties.getProperty("application.name", NettyServerTemplate.DEFAULT_NAME);

		String instanceId = serverProperties.getProperty("consul.instanceId");
		if (StringUtil.isNullOrEmpty(instanceId)) {
			instanceId = applicationName + "-" + applicationPort;
		}
		this.instanceId = instanceId;

		scheme = serverProperties.getProperty("consul.scheme", DEFAULT_SCHEME);
		preferIpAddress = Boolean.parseBoolean(serverProperties.getProperty("consul.preferIpAddress", "false"));
		InetAddress inetAddress = InetUtil.findFirstNonLoopbackAddress(null);
		this.ipAddress = inetAddress.getHostAddress();
		this.hostname = inetAddress.getHostName();

		healthCheckUrl = serverProperties.getProperty("consul.healthCheckUrl");
		healthCheckInterval = serverProperties.getProperty("consul.healthCheckInterval", "10s");
		healthCheckTimeout = serverProperties.getProperty("consul.healthCheckTimeout");
		if (StringUtil.isNullOrEmpty(healthCheckUrl)) {
			healthCheckPath = serverProperties.getProperty("consul.healthCheckPath", DEFAULT_HEALTHCHECK_PATH);
			healthCheckUrl = String.format("%s://%s:%s%s", scheme, getHostname(), applicationPort, healthCheckPath);
		}
	}

	public ApplicationProperties(List<Source> sourceList, Integer serverPort) {
		if (serverPort != null && serverPort > 0) {
			serverProperties.put("application.port", serverPort);
		}

		if (sourceList != null && sourceList.size() > 0) {
			for (int i = sourceList.size() - 1; i >= 0; i--) {
				Source source = sourceList.get(i);
				for (Object key : source.keySet()) {
					serverProperties.put(key, source.getProperty((String) key));
				}
			}
		}
		host = serverProperties.getProperty("consul.host", DEFAULT_IP);
		port = Integer.parseInt(serverProperties.getProperty("consul.port", DEFAULT_PORT));
		applicationPort = Integer.parseInt(serverProperties.getProperty("application.port", DEFAULT_APP_PORT));
		applicationName = serverProperties.getProperty("application.name", NettyServerTemplate.DEFAULT_NAME);

		String instanceId = serverProperties.getProperty("consul.instanceId");
		if (StringUtil.isNullOrEmpty(instanceId)) {
			instanceId = applicationName + "-" + applicationPort;
		}
		this.instanceId = instanceId;

		scheme = serverProperties.getProperty("consul.scheme", DEFAULT_SCHEME);
		preferIpAddress = Boolean.parseBoolean(serverProperties.getProperty("consul.preferIpAddress", "false"));
		InetAddress inetAddress = InetUtil.findFirstNonLoopbackAddress(null);
		this.ipAddress = inetAddress.getHostAddress();
		this.hostname = inetAddress.getHostName();

		healthCheckUrl = serverProperties.getProperty("consul.healthCheckUrl");
		healthCheckInterval = serverProperties.getProperty("consul.healthCheckInterval", "10s");
		healthCheckTimeout = serverProperties.getProperty("consul.healthCheckTimeout");
		if (StringUtil.isNullOrEmpty(healthCheckUrl)) {
			healthCheckPath = serverProperties.getProperty("consul.healthCheckPath", DEFAULT_HEALTHCHECK_PATH);
			healthCheckUrl = String.format("%s://%s:%s%s", scheme, getHostname(), applicationPort, healthCheckPath);
		}
	}

	public String getHostname() {
		return this.preferIpAddress ? this.ipAddress : this.hostname;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getApplicationPort() {
		return applicationPort;
	}

	public void setApplicationPort(int applicationPort) {
		this.applicationPort = applicationPort;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getHealthCheckPath() {
		return healthCheckPath;
	}

	public boolean isHealthCheckPath(String url) {
		if (StringUtil.isNullOrEmpty(healthCheckPath)) {
			return false;
		}
		if (healthCheckPath.equals(url)) {
			return true;
		}
		return false;
	}

	public void setHealthCheckPath(String healthCheckPath) {
		this.healthCheckPath = healthCheckPath;
	}

	public String getHealthCheckUrl() {
		return healthCheckUrl;
	}

	public void setHealthCheckUrl(String healthCheckUrl) {
		this.healthCheckUrl = healthCheckUrl;
	}

	public String getHealthCheckInterval() {
		return healthCheckInterval;
	}

	public void setHealthCheckInterval(String healthCheckInterval) {
		this.healthCheckInterval = healthCheckInterval;
	}

	public String getHealthCheckTimeout() {
		return healthCheckTimeout;
	}

	public void setHealthCheckTimeout(String healthCheckTimeout) {
		this.healthCheckTimeout = healthCheckTimeout;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getString(String key) {
		return serverProperties.getProperty(key);
	}

	public int getInt(String key) {
		String value = serverProperties.getProperty(key);
		if (StringUtil.isNullOrEmpty(value)) {
			return 0;
		} else {
			return Integer.parseInt(value);
		}
	}

	public boolean getBoolean(String key) {
		String value = serverProperties.getProperty(key);
		if (StringUtil.isNullOrEmpty(value)) {
			return false;
		} else {
			return Boolean.parseBoolean(value);
		}
	}

	public List<String> getList(String key) {
		String value = serverProperties.getProperty(key);
		if (StringUtil.isNullOrEmpty(value)) {
			return null;
		} else {
			List<String> list = Arrays.asList(value.split(","));
			return list;
		}
	}
}
