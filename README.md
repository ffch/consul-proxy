[![License](http://img.shields.io/:license-apache-blue.svg "2.0")](http://www.apache.org/licenses/LICENSE-2.0.html)
[![JDK 1.8](https://img.shields.io/badge/JDK-1.8-green.svg "JDK 1.8")]()
[![Maven Central](https://img.shields.io/maven-central/v/cn.pomit/consul-proxy.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22cn.pomit%22%20AND%20a:%22consul-proxy%22)

## Consul-proxy项目简介

Springcloud+consul作为微服务的注册已经见怪不怪了，试下也很流行，在我个人云服务器上，我也是这样做的。

然而，我的云服务器内存比较小，很快内存就被cloud全家桶吃光了，没办法部署其他应用了，因此，我觉得将一些服务独立出去，放弃cloud全家桶。

Consul-proxy使用netty+consul实现服务注册，并提供了若干简单的注解实现了http的mapping映射处理。

## [Gitee](https://gitee.com/ffch/consul-proxy)
## [Github](https://github.com/ffch/consul-proxy)
## [Get Started](https://www.pomit.cn/consul-proxy/)

## 主要功能

 1. 快速启动。
 2. 映射路径。
 3. handler中的属性注入。
 4. 多handler支持，类似于spring的Controller。
 5. 支持springboot的server.port和spring.profiles.active多配置文件 (V1.1版本)
 6. 支持@EnableMybatis注解，快速使用mybatis(V1.2版本)
 7. 新增@InitConfiguration注解，注解在启动类上，将自动加载注解指定类的initConfiguration方法并传递属性文件。(V1.3版本)
 8. Netty在Json请求时，如果解析key-value参数会出现空指针异常，因此Json请求不再解析body的参数，直接返回body内容。(V1.3版本)

## 使用说明

jar包已经上传到maven中央仓库。
https://search.maven.org/search?q=consul-proxy ，groupId为cn.pomit。

[使用文档地址](https://www.pomit.cn/consul-proxy)

### maven依赖

```xml
<dependency>
	<groupId>cn.pomit</groupId>
	<artifactId>consul-proxy</artifactId>
	<version>1.3</version>
</dependency>
```

### 启动

使用注解@JsonServer启动，可以指定端口和handler处理逻辑。

```java
@JsonServer(handler=AlarmHandler.class)
public class AlarmApp {
	public static void main(String[] args) {
		ConsulProxyApplication.run(AlarmApp.class);
	}

}
```

若使用mybatis-proxy，可以如下启动：

```java
@JsonServer(handler=AlarmHandler.class)
@EnableMybatis(mapperScan = "cn.pomit.alarm.mapper")
public class AlarmApp {
	public static void main(String[] args) {
		ConsulProxyApplication.run(AlarmApp.class);
	}

}
```

若需要将属性传递给某个类进行初始化，可以在启动类上加上：

```java
import cn.pomit.consul.ConsulProxyApplication;
import cn.pomit.consul.annotation.EnableServer;
import cn.pomit.consul.annotation.InitConfiguration;
import cn.pomit.serv.config.DataSourceConfiguration;
import cn.pomit.serv.config.MailConfiguration;
import cn.pomit.serv.handler.AdviceHandler;
import cn.pomit.serv.handler.EmailRestHandler;

@EnableServer(handler = { EmailRestHandler.class,AdviceHandler.class })
@InitConfiguration(configurations = { DataSourceConfiguration.class })
public class ServiceApp {
	public static void main(String[] args) {
		ConsulProxyApplication.run(ServiceApp.class, args);
	}

}
```
这里，新建了个DataSourceConfiguration，用户替换mybatis的数据源，因此就不需要使用EnableMybatis注解了。

DataSourceConfiguration需要配置Mybatis初始化,调用MybatisConfiguration.initConfiguration进行初始化。

DataSourceConfiguration：
```java
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;

import cn.pomit.mybatis.configuration.MybatisConfiguration;

public class DataSourceConfiguration {
	public static final String DATASOURCE_PREFIX = "datasource.";
	public static void initConfiguration(Properties properties) {
		String packageName = "cn.pomit.serv.mapper";
		try {
			Properties dataSourceProperties = new Properties();
			for (Object key : properties.keySet()) {
				String tmpKey = key.toString();
				if(tmpKey.startsWith(DATASOURCE_PREFIX)){
					String datasourceKey = tmpKey.replace(DATASOURCE_PREFIX, "");
					dataSourceProperties.put(datasourceKey, properties.get(key));
				}
			}
			DataSource dataSource = BasicDataSourceFactory.createDataSource(dataSourceProperties);
			MybatisConfiguration.initConfiguration(packageName, dataSource);
		} catch (Exception e) {
			e.printStackTrace();
			MybatisConfiguration.initConfiguration(packageName, properties);
		}
	}

	
}
```

### 业务逻辑

继承AbstractResourceHandler的handler可以实现业务逻辑。

#### 属性注入

handler中可以使用Value注解进行属性注入：

```java
@Value("api.gateway.kongUrl")
private String apiGatewayKongUrl;
```

#### 路径映射

handler中可以使用Mapping注解进行路径映射：

```java
@Mapping("/alarm/gateway")
```

#### 多配置文件（V1.1版本）

可以在命令行使用server.port。

可以使用spring.profiles.active或者profiles.active指定多个配置文件。


## 性能测试
调用demo项目中的hosts接口对cloud和netty进行对比

cloud和netty最大连接数分别设置为10000，最大线程数设置为200.

对比结果如下：

|       | 内存 |
| ----- | ---- |
| cloud | 185m |
| netty | 117m |


|| 采样值 |平均时间|偏离 |异常率|
|-----|----|----|----|----|
|cloud| 1000 | 3128 |1888|0|
|netty| 1000 | 2215 |2072|0|
|cloud| 3000 | 3416 |2882|0|
|netty| 3000 | 1925 |1647|0|
|cloud| 20000 | 15653 |7591|0.50605|
|netty| 20000 | 11737 |7076|0.2289|

## 示例

启动：

```java
package cn.pomit.alarm;

import cn.pomit.alarm.handler.FalconAlarmHandler;
import cn.pomit.alarm.handler.GatewayAlarmHandler;
import cn.pomit.consul.ConsulProxyApplication;
import cn.pomit.consul.annotation.EnableServer;

@EnableServer(handler={FalconAlarmHandler.class,GatewayAlarmHandler.class})
public class AlarmApp {
	public static void main(String[] args) {
		ConsulProxyApplication.run(AlarmApp.class);
	}
}

```

handler：

```java
public class GatewayAlarmHandler extends AbstractResourceHandler {
	@Value("api.gateway.kongUrl")
	private String apiGatewayKongUrl;

	@Value("api.gateway.appKey")
	private String apiGatewayAppKey;

	@Value("api.gateway.appSecret")
	private String apiGatewayAppSecret;

	@Mapping("/alarm/gateway")
	public HttpResponseMessage gateway(HttpRequestMessage httpRequestMessage) {
		try {
			log.info("apiGatewayAppSecret: " + apiGatewayAppSecret + ", apiGatewayAppKey: " + apiGatewayAppKey);

			ApiGatewayClient apiGatewayClient = new ApiGatewayClient.Builder().kongUrl(apiGatewayKongUrl)
					.appKey(apiGatewayAppKey).appSecret(apiGatewayAppSecret).client(HttpClientPrototype.getHttpClient())
					.build();

			ApiGatewayService apiGatewayService = new ApiGatewayService(apiGatewayClient);
			ResultModel resultModel = apiGatewayService.getAlarmInfo(null);
			HttpResponseMessage httpResponseMessage = new HttpResponseMessage();
			httpResponseMessage.setResCode(ResCode.OK.getValue());
			httpResponseMessage.setResType(ResType.JSON.getValue());
			httpResponseMessage.setMessage(JSONObject.toJSONString(resultModel));
			return httpResponseMessage;
		} catch (Exception e) {
			e.printStackTrace();
			HttpResponseMessage httpResponseMessage = new HttpResponseMessage();
			httpResponseMessage.setResCode(ResCode.OK.getValue());
			httpResponseMessage.setResType(ResType.JSON.getValue());
			httpResponseMessage.setMessage(JSONObject.toJSONString(ResultModel.error("请求API网关失败")));
			return httpResponseMessage;
		}
	}

	@Mapping("/health")
	public HttpResponseMessage health(HttpRequestMessage httpRequestMessage) {
		HttpResponseMessage httpResponseMessage = new HttpResponseMessage();
		httpResponseMessage.setResCode(ResCode.OK.getValue());
		httpResponseMessage.setResType(ResType.TEXT.getValue());
		httpResponseMessage.setMessage("操作成功！");
		return httpResponseMessage;
	}

	public String getApiGatewayKongUrl() {
		return apiGatewayKongUrl;
	}

	public void setApiGatewayKongUrl(String apiGatewayKongUrl) {
		this.apiGatewayKongUrl = apiGatewayKongUrl;
	}

	public String getApiGatewayAppKey() {
		return apiGatewayAppKey;
	}

	public void setApiGatewayAppKey(String apiGatewayAppKey) {
		this.apiGatewayAppKey = apiGatewayAppKey;
	}

	public String getApiGatewayAppSecret() {
		return apiGatewayAppSecret;
	}

	public void setApiGatewayAppSecret(String apiGatewayAppSecret) {
		this.apiGatewayAppSecret = apiGatewayAppSecret;
	}

}
```

## Demo项目

## [Gitee-Consul-proxy-test](https://gitee.com/ffch/consul-proxy-test)
## [Github-Consul-proxy-test](https://github.com/ffch/consul-proxy-test)
## [Get-Started](https://www.pomit.cn/consul-proxy)

## 版权声明
consul-proxy使用 Apache License 2.0 协议.

## 作者信息
      
   作者博客：https://blog.csdn.net/feiyangtianyao
  
  个人网站：https://www.pomit.cn
 
   作者邮箱： fufeixiaoyu@163.com

## License
Apache License V2

