package cn.pomit.consul.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.pomit.consul.http.HttpRequestMessage;
import cn.pomit.consul.http.HttpResponseMessage;

public class DefaultResourceHandler extends ResourceHandler {
	private final Log log = LogFactory.getLog(getClass());

	@Override
	public HttpResponseMessage handle(HttpRequestMessage httpRequestMessage) {
		log.debug(httpRequestMessage);

		HttpResponseMessage httpResponseMessage = new HttpResponseMessage();
		httpResponseMessage.setResCode(HttpResponseMessage.ResCode.OK.getValue());
		httpResponseMessage.setResType(HttpResponseMessage.ResType.TEXT.getValue());
		httpResponseMessage.setMessage("操作成功！");
		return httpResponseMessage;
	}

}
