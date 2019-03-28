package cn.pomit.consul.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.pomit.consul.config.ApplicationProperties;
import cn.pomit.consul.http.HttpRequestMessage;
import cn.pomit.consul.http.HttpResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.internal.StringUtil;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	private ResourceHandler resourceHandler = null;
	private ApplicationProperties consulProperties;
	private final Log log = LogFactory.getLog(getClass());

	public HttpServerHandler(ResourceHandler resourceHandler, ApplicationProperties consulProperties) {
		this.resourceHandler = resourceHandler;
		this.consulProperties = consulProperties;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
		try {
			if (resourceHandler == null)
				resourceHandler = new DefaultResourceHandler();

			HttpRequest httpRequest = (HttpRequest) msg;
			HttpRequestMessage httpRequestMessage = new HttpRequestMessage(httpRequest);
			httpRequestMessage.parseRequest();

			log.info("收到请求：" + httpRequestMessage.getUrl());
			HttpResponseMessage httpResponseMessage = httpRequestMessage.getReponse();

			httpResponseMessage = resourceHandler.handle(httpRequestMessage);
			if (httpResponseMessage == null) {
				if (consulProperties != null && consulProperties.isHealthCheckPath(httpRequestMessage.getUrl())) {
					httpResponseMessage = new HttpResponseMessage();
					httpResponseMessage.setResCode(HttpResponseMessage.ResCode.OK.getValue());
					httpResponseMessage.setResType(HttpResponseMessage.ResType.TEXT.getValue());
				}else{
					httpResponseMessage = new HttpResponseMessage();
					httpResponseMessage.setResType(HttpResponseMessage.ResType.TEXT.getValue());
					httpResponseMessage.setResCode(HttpResponseMessage.ResCode.INTERNAL_ERROR.getValue());
					httpResponseMessage.setMessage("内部错误！");
				}	
			}
			log.info("响应数据：" + httpResponseMessage);
			ctx.writeAndFlush(httpResponseMessage);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getStackTrace()[0] + "---" + e.getMessage());
			HttpResponseMessage httpResponseMessage = new HttpResponseMessage();
			httpResponseMessage.setResType(HttpResponseMessage.ResType.TEXT.getValue());
			httpResponseMessage.setResCode(HttpResponseMessage.ResCode.INTERNAL_ERROR.getValue());
			httpResponseMessage.setMessage(e.getMessage());
			ctx.writeAndFlush(httpResponseMessage);
		}
	}
}
