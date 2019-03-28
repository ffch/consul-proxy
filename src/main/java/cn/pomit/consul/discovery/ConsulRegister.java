package cn.pomit.consul.discovery;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

import cn.pomit.consul.config.ApplicationProperties;

public class ConsulRegister {
	private ApplicationProperties consulProperties = null;
	private final Log log = LogFactory.getLog(getClass());

	public ConsulRegister(ApplicationProperties consulProperties) {
		this.consulProperties = consulProperties;
	}

	public void register() {
		log.info("注册服务到:" + consulProperties.getHost() + ":" + consulProperties.getPort());
		ConsulClient client = new ConsulClient(consulProperties.getHost(), consulProperties.getPort());
		NewService service = new NewService();
		service.setId(consulProperties.getInstanceId());
		service.setAddress(consulProperties.getHostname());
		service.setName(consulProperties.getApplicationName());
		service.setTags(createTags());

		service.setPort(consulProperties.getApplicationPort());
		setCheck(service);
		try {
			client.agentServiceRegister(service);
		} catch (ConsulException e) {
			log.warn("Error registering service with consul: " + service, e);
		}
		client.agentServiceRegister(service);
		log.info("服务已注册：" + service);
	}

	private void setCheck(NewService service) {
		NewService.Check check = new NewService.Check();

		String healthCheckUrl = consulProperties.getHealthCheckUrl();

		check.setHttp(healthCheckUrl);

		check.setInterval(consulProperties.getHealthCheckInterval());
		check.setTimeout(consulProperties.getHealthCheckTimeout());
		service.setCheck(check);
	}

	private List<String> createTags() {
		List<String> tags = new LinkedList<>(consulProperties.getTags());

		// store the secure flag in the tags so that clients will be able to
		// figure out whether to use http or https automatically
		tags.add("secure=" + (consulProperties.getScheme().equalsIgnoreCase("https")));

		return tags;
	}
}
