package cn.pomit.consul.endpoint;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.discovery.ConsulRegister;
import cn.pomit.consul.handler.HttpServerHandler;
import cn.pomit.consul.handler.ResourceHandler;
import cn.pomit.consul.handler.codec.FullHttpResponseEncoder;
import cn.pomit.consul.util.PropertyUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public abstract class NettyServerTemplate {
	protected Integer port = null;
	protected String name = null;
	public static String DEFAULT_NAME = "JsonServer";
	protected static String PROPERTIES_NAME = "application.properties";
	protected String charset = "UTF-8";
	private final Log log = LogFactory.getLog(getClass());
	static private EventLoopGroup bossGroup = new NioEventLoopGroup();
	static private EventLoopGroup workerGroup = new NioEventLoopGroup();
	protected ApplicationProperties consulProperties = null;

	NettyServerTemplate() {
		Properties properties = null;
		try {
			properties = PropertyUtil.getProperties(PROPERTIES_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		consulProperties = new ApplicationProperties(properties, port);

		port = consulProperties.getApplicationPort();
		name = consulProperties.getApplicationName();
	}

	protected ChannelHandler[] createHandlers() throws Exception {
		return new ChannelHandler[] { new HttpResponseEncoder(), new HttpRequestDecoder(),
				new HttpObjectAggregator(1048576), new FullHttpResponseEncoder(charset),
				new HttpServerHandler(resourceHandler(), consulProperties) };
	}

	public void start() throws Exception {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelHandler[] handlers = createHandlers();
						for (ChannelHandler handler : handlers) {
							ch.pipeline().addLast(handler);
						}
					}
				});

		ChannelFuture cf = b.bind(port).await();
		if (!cf.isSuccess()) {
			log.error("无法绑定端口：" + port);
			throw new Exception("无法绑定端口：" + port);
		}

		log.info("服务[{" + name + "}]启动完毕，监听端口[{" + port + "}]");

		ConsulRegister consulRegister = new ConsulRegister(consulProperties);
		consulRegister.register();
	}

	public void stop() {
		bossGroup.shutdownGracefully().syncUninterruptibly();
		workerGroup.shutdownGracefully().syncUninterruptibly();
		log.info("服务[{" + name + "}]关闭。");
	}

	abstract protected ResourceHandler resourceHandler() throws Exception;
}
