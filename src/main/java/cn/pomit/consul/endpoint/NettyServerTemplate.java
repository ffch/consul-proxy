package cn.pomit.consul.endpoint;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.discovery.ConsulRegister;
import cn.pomit.consul.handler.HttpServerHandler;
import cn.pomit.consul.handler.ResourceServerHandler;
import cn.pomit.consul.handler.codec.FullHttpResponseEncoder;
import cn.pomit.consul.handler.resource.AbstractResourceHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public abstract class NettyServerTemplate {
	protected List<Class<? extends AbstractResourceHandler>> resourceHandlerList = null;
	private static int httpIdleTime = 120;
	protected Integer port = null;
	protected String name = null;
	public static String DEFAULT_NAME = "JsonServer";
	protected static String PROPERTIES_NAME = "application.properties";
	protected String charset = "UTF-8";
	private final Logger log = LoggerFactory.getLogger(getClass());

	static private EventLoopGroup bossGroup = new NioEventLoopGroup();
	static private EventLoopGroup workerGroup = new NioEventLoopGroup();
	protected ApplicationProperties consulProperties = null;
	protected ResourceServerHandler resourceServerHandler = null;

	NettyServerTemplate() {
		
	}

	protected ChannelHandler[] createHandlers() throws Exception {
		resourceServerHandler = resourceHandler();
		return new ChannelHandler[] { new IdleStateHandler(0, 0, httpIdleTime), new HttpResponseEncoder(), new HttpRequestDecoder(),
				new HttpObjectAggregator(1048576), new FullHttpResponseEncoder(charset),
				new HttpServerHandler(resourceServerHandler) };
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
				}).option(ChannelOption.SO_BACKLOG, 1024)
				.option(ChannelOption.SO_REUSEADDR, true)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.SO_REUSEADDR, true);

		ChannelFuture cf = b.bind(port).await();
		if (!cf.isSuccess()) {
			log.error("无法绑定端口：{}", port);
			throw new Exception("无法绑定端口：" + port);
		}
		log.info("server启动完毕，开始注册服务");

		ConsulRegister consulRegister = new ConsulRegister(consulProperties);
		consulRegister.register();

		log.info("服务[{}]启动完毕，监听端口[{}]", name, port);
	}

	public void stop() {
		bossGroup.shutdownGracefully().syncUninterruptibly();
		workerGroup.shutdownGracefully().syncUninterruptibly();
		log.info("服务[{}]关闭。", name);
	}

	public void setResourceHandlers(Class<? extends AbstractResourceHandler> resourceHandler[]) throws Exception {
		this.resourceHandlerList = Arrays.asList(resourceHandler);
		ResourceServerHandler.initInstance(resourceHandlerList, consulProperties);

	}

	abstract protected ResourceServerHandler resourceHandler() throws Exception;
}
