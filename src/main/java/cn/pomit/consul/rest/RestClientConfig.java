package cn.pomit.consul.rest;

import cn.pomit.consul.config.ApplicationProperties;

/**
 * 
 * @author fufei
 *
 */
public class RestClientConfig {
	public static final String OKTTP_TYPE = "okhttp";
	public static final String HTTPCLIENT_TYPE = "httpclient";
	public static final String NO_TYPE = "none";
	private String httpType = OKTTP_TYPE;
	private static final int DEFAULT_TIMEOUT = 5000;
	private int timeout = DEFAULT_TIMEOUT;
	private boolean retry = false;
	private static final int DEFAULT_RETRY_TIMES = 3;
	private int retryTimes = DEFAULT_RETRY_TIMES;
	private PoolConfig poolConfig;

	public RestClientConfig(ApplicationProperties applicationProperties) {
		httpType = applicationProperties.getString("rest.client.type");
		if (httpType == null) {
			httpType = OKTTP_TYPE;
		}
		timeout = applicationProperties.getInt("rest.client.timeout", DEFAULT_TIMEOUT);
		retry = applicationProperties.getBoolean("rest.client.retry", false);
		retryTimes = applicationProperties.getInt("rest.client.retry.times", DEFAULT_RETRY_TIMES);
		poolConfig = new PoolConfig();
		poolConfig.setEnable(applicationProperties.getBoolean("rest.client.pool.enable", true));
		poolConfig.setMaxIdle(applicationProperties.getInt("rest.client.pool.maxIdle", PoolConfig.DEFAULT_MAX_IDLE));
	}

	public String getHttpType() {
		return httpType;
	}

	public void setHttpType(String httpType) {
		this.httpType = httpType;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isRetry() {
		return retry;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	public int getPoolMaxIdle() {
		return poolConfig.getMaxIdle();
	}

	public boolean isEnablePool() {
		return poolConfig.isEnable();
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	static class PoolConfig {
		private static final int DEFAULT_MAX_IDLE = 20;
		private int maxIdle = 10;
		private boolean enable;

		public int getMaxIdle() {
			return maxIdle;
		}

		public void setMaxIdle(int maxIdle) {
			this.maxIdle = maxIdle;
		}

		public boolean isEnable() {
			return enable;
		}

		public void setEnable(boolean enable) {
			this.enable = enable;
		}

	}

}
