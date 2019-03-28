package cn.pomit.consul.handler.codec;

import java.util.List;

import cn.pomit.consul.http.HttpResponseMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class FullHttpResponseEncoder extends MessageToMessageEncoder<HttpResponseMessage> {
	private String charset;

	public FullHttpResponseEncoder(String charset) {
		super();
		this.charset = charset;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, HttpResponseMessage message, List<Object> out) {
		try {
			DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.valueOf(message.getResCode()),
					Unpooled.wrappedBuffer(message.getMessage().getBytes(charset)));
			response.headers().add(HttpHeaderNames.CONTENT_TYPE, message.getResType() + ";charset=" + charset);
			response.headers().add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
			if (message.getHeaders() != null && message.getHeaders().size() > 0) {
				for (HttpHeaders headers : message.getHeaders())
					response.headers().add(headers);
			}
			if (message.getEncodedCookie() != null && message.getEncodedCookie().size() > 0) {
				for (String cookie : message.getEncodedCookie())
					response.headers().add(HttpHeaderNames.SET_COOKIE, cookie);
			}
			if (message.getResCode() == HttpResponseMessage.ResCode.REDIRECT.getValue()) {
				response.headers().add(HttpHeaderNames.LOCATION, message.getRedirectUrl());
			}

			out.add(response);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
