package cn.pomit.consul;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.pomit.consul.annotation.EnableMybatis;
import cn.pomit.consul.annotation.EnableServer;
import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.config.DefaultSource;
import cn.pomit.consul.config.PropertySource;
import cn.pomit.consul.config.Source;
import cn.pomit.consul.endpoint.JsonHttpServer;
import cn.pomit.consul.util.PropertyUtil;
import io.netty.util.internal.StringUtil;

public class ConsulProxyApplication {
	protected static String PROPERTIES_NAME = "application.properties";
	protected static String PROPERTIES_ENV_NAME = "application-%s.properties";
	protected static String SPRING_ENV = "spring.profiles.active";
	protected static String APPLICATION_ENV = "profiles.active";
	protected static String SERVER_PORT = "server.port";
	protected static String APPLICATION_PORT = "application.port";

	protected static String DATABASE_CONFIG_CLASS = "mybatis.configuration";

	private static Logger log = LoggerFactory.getLogger(ConsulProxyApplication.class);

	public static void run(Class<?> app, String[] args) {
		try {
			List<Source> sourceList = initProperties(args);
			EnableServer jsonServer = app.getAnnotation(EnableServer.class);
			int port = jsonServer.port();
			ApplicationProperties consulProperties = new ApplicationProperties(sourceList, port);
			JsonHttpServer defaultJsonServer = new JsonHttpServer(consulProperties);

			defaultJsonServer.setResourceHandlers(jsonServer.handler());
			defaultJsonServer.start();

			EnableMybatis enableMybatis = app.getAnnotation(EnableMybatis.class);
			if (enableMybatis != null) {
				String className = consulProperties.getString(DATABASE_CONFIG_CLASS);
				if (StringUtil.isNullOrEmpty(className)) {
					log.error("未找到myabtsi的配置信息");
					throw new Exception("未找到myabtsi的配置信息");
				}
				Class<?> cls = Class.forName(className);
				String mapperScan = enableMybatis.mapperScan();

				Object obj = null;
				if (!StringUtil.isNullOrEmpty(mapperScan)) {
					obj = cls.getDeclaredConstructor(String.class, Properties.class).newInstance(mapperScan,
							consulProperties.getServerProperties());
				} else {
					obj = cls.getDeclaredConstructor(Properties.class)
							.newInstance(consulProperties.getServerProperties());
				}
			}
		} catch (IOException e) {
			log.error("读取配置文件失败！", e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<Source> initProperties(String[] args) throws IOException {
		List<Source> list = new ArrayList<>();
		DefaultSource defaultSource = new DefaultSource();
		for (String arg : args) {
			if (arg.startsWith("--")) {
				String optionText = arg.substring(2, arg.length());
				String optionName;
				String optionValue = null;
				if (optionText.contains("=")) {
					optionName = optionText.substring(0, optionText.indexOf('='));
					optionValue = optionText.substring(optionText.indexOf('=') + 1, optionText.length());
				} else {
					optionName = optionText;
				}
				if (optionName.isEmpty() || (optionValue != null && optionValue.isEmpty())) {
					throw new IllegalArgumentException("Invalid argument syntax: " + arg);
				}
				defaultSource.setProperty(optionName, optionValue);
			}
		}
		if (defaultSource.getProperty(SERVER_PORT) != null) {
			defaultSource.setProperty(APPLICATION_PORT, defaultSource.getProperty(SERVER_PORT));
		}
		list.add(defaultSource);

		Properties properties = PropertyUtil.getProperties(PROPERTIES_NAME);
		if (properties == null)
			properties = new Properties();
		PropertySource propertySource = new PropertySource(properties);
		if (propertySource.getProperty(SERVER_PORT) != null) {
			propertySource.setProperty(APPLICATION_PORT, propertySource.getProperty(SERVER_PORT));
		}
		list.add(propertySource);

		String env = defaultSource.getProperty(SPRING_ENV);
		if (StringUtil.isNullOrEmpty(env)) {
			env = defaultSource.getProperty(APPLICATION_ENV);
			if (StringUtil.isNullOrEmpty(env)) {
				env = StringUtil.isNullOrEmpty(propertySource.getProperty(SPRING_ENV))
						? propertySource.getProperty(APPLICATION_ENV) : propertySource.getProperty(SPRING_ENV);
			}
		}

		if (!StringUtil.isNullOrEmpty(env)) {
			Properties envProperties = PropertyUtil.getProperties(String.format(PROPERTIES_ENV_NAME, env));
			if (envProperties == null) {
				envProperties = PropertyUtil.getProperties("config/" + String.format(PROPERTIES_ENV_NAME, env));
			}
			if (envProperties != null) {
				PropertySource envSource = new PropertySource(envProperties);
				list.add(envSource);
			}
		}
		return list;
	}
}
