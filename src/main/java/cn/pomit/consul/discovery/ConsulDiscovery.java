package cn.pomit.consul.discovery;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

import cn.pomit.consul.config.ApplicationProperties;

public class ConsulDiscovery {
	private ApplicationProperties consulProperties = null;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public ConsulDiscovery(ApplicationProperties consulProperties) {
		this.consulProperties = consulProperties;
	}

	public Server discovery(String serviceId) {
		try {
			log.info("查找注册服务{}的机器列表", serviceId);
			ConsulClient client = new ConsulClient(consulProperties.getHost(), consulProperties.getPort());

			Response<List<HealthService>> catalogServiceList = client.getHealthServices(serviceId, true, null);
			List<HealthService> list = catalogServiceList.getValue();
			if (list == null || list.isEmpty())
				return null;

			ILoadBalancer balancer = new BaseLoadBalancer();

			List<Server> servers = new ArrayList<Server>();
			for (HealthService item : list) {
				log.info("服务{}注册的机器有：{}", serviceId, item.getService().toString());
				servers.add(new Server(item.getService().getAddress(), item.getService().getPort()));
			}
			balancer.addServers(servers);

			Server choosedServer = balancer.chooseServer(null);
			log.info("服务{}选择的机器是：{}", serviceId, choosedServer);

			return choosedServer;
		} catch (Exception e) {
			return null;
		}
	}

}
