package cn.pomit.consul.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.pomit.consul.annotation.Mapping;
import cn.pomit.consul.http.HttpRequestMessage;
import cn.pomit.consul.http.HttpResponseMessage;
import cn.pomit.consul.http.res.ResCode;
import cn.pomit.consul.http.res.ResType;

public class DefaultResourceHandler extends AbstractResourceHandler {
	private final Log log = LogFactory.getLog(getClass());

	@Mapping("/**")
	public HttpResponseMessage all(HttpRequestMessage httpRequestMessage){
		log.debug(httpRequestMessage);

		HttpResponseMessage httpResponseMessage = new HttpResponseMessage();
		httpResponseMessage.setResCode(ResCode.OK.getValue());
		httpResponseMessage.setResType(ResType.TEXT.getValue());
		httpResponseMessage.setMessage("操作成功！");
		return httpResponseMessage;
	}
}
