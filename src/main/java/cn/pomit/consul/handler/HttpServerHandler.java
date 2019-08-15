package cn.pomit.consul.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.pomit.consul.http.HttpRequestMessage;
import cn.pomit.consul.http.HttpResponseMessage;
import cn.pomit.consul.http.res.ResCode;
import cn.pomit.consul.http.res.ResType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	private ResourceServerHandler resourceHandler = null;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public HttpServerHandler(ResourceServerHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
		try {

			HttpRequest httpRequest = (HttpRequest) msg;
			HttpRequestMessage httpRequestMessage = new HttpRequestMessage(httpRequest);
			httpRequestMessage.parseRequest();

			log.debug("收到请求：" + httpRequestMessage.getUrl());
			HttpResponseMessage httpResponseMessage = httpRequestMessage.getReponse();

			httpResponseMessage = resourceHandler.handle(httpRequestMessage);
			if (httpResponseMessage == null) {
				log.error("未找到{}的路径映射信息！", httpRequestMessage.getUrl());
				httpResponseMessage = new HttpResponseMessage();
				httpResponseMessage.setResType(ResType.TEXT.getValue());
				httpResponseMessage.setResCode(ResCode.INTERNAL_ERROR.getValue());
				httpResponseMessage.setMessage("未找到相应的路径映射信息！");
			}
			log.trace("响应数据：" + httpResponseMessage);
			ctx.writeAndFlush(httpResponseMessage);
			return;
		} catch (Throwable e) {
			e.printStackTrace();
			log.error(e.getStackTrace()[0] + "---" + e.getMessage());
			HttpResponseMessage httpResponseMessage = new HttpResponseMessage();
			httpResponseMessage.setResType(ResType.TEXT.getValue());
			httpResponseMessage.setResCode(ResCode.INTERNAL_ERROR.getValue());
			httpResponseMessage.setMessage("异常抛出：" + e.getMessage());
			ctx.writeAndFlush(httpResponseMessage);
		}
	}
}
