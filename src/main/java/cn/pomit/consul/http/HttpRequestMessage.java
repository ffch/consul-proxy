package cn.pomit.consul.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.util.TypeUtils;

import cn.pomit.consul.http.res.ResCode;
import cn.pomit.consul.http.res.ResType;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.internal.StringUtil;

public class HttpRequestMessage extends DefaultHttpRequest {
	private Map<String, Object> params = null;
	private String content = null;
	private String url = "";
	private HttpRequest hr = null;
	private Map<String, Cookie> cookies = new HashMap<>();
	private HttpResponseMessage hrm = new HttpResponseMessage();
	private Charset charset = Charset.defaultCharset();

	public HttpRequestMessage(HttpVersion httpVersion, HttpMethod method, String uri) {
		super(httpVersion, method, uri);
		hr = this;
	}

	public HttpRequestMessage(DefaultHttpRequest defaultHttpRequest) {
		super(defaultHttpRequest.protocolVersion(), defaultHttpRequest.method(), defaultHttpRequest.uri(),
				defaultHttpRequest.headers());
		this.hr = defaultHttpRequest;
	}

	public HttpRequestMessage(DefaultHttpRequest defaultHttpRequest, Map<String, Object> params) {
		super(defaultHttpRequest.protocolVersion(), defaultHttpRequest.method(), defaultHttpRequest.uri(),
				defaultHttpRequest.headers());
		this.params = params;
	}

	public HttpRequestMessage(HttpRequest hr) {
		super(hr.protocolVersion(), hr.method(), hr.uri(), hr.headers());
		this.hr = hr;
	}

	public Object getParameter(String key) {
		return params.get(key);
	}

	public String getParameterString(String key) {
		return params.get(key) == null ? null : params.get(key).toString();
	}

	public int getParameterInt(String key) {
		return TypeUtils.cast(params.get(key), int.class, null);
	}

	public boolean getParameterBoolean(String key) {
		return TypeUtils.cast(params.get(key), boolean.class, null);
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public HttpResponseMessage getReponse() {
		return hrm;
	}

	public String getUrl() {
		return url;
	}

	public HttpResponseMessage getReponse(ResCode resCode, ResType resType, String message) {
		hrm.setResCode(resCode.getValue());
		hrm.setResType(resType.getValue());
		hrm.setMessage(message);
		return hrm;
	}

	public void parseRequest() {
		try {
			URI uri = new URI(uri());
			setUrl(uri.getPath());

			String cookieStr = this.headers().get("Cookie");
			if (!StringUtil.isNullOrEmpty(cookieStr)) {
				Set<Cookie> cookiesSet = ServerCookieDecoder.LAX.decode(cookieStr);
				if (cookiesSet != null && cookiesSet.size() > 0) {
					Iterator<Cookie> it = cookiesSet.iterator();
					while (it.hasNext()) {
						Cookie cookie = it.next();
						cookies.put(cookie.name(), cookie);
					}
				}
			}

			if (uri.getQuery() != null && !"".equals(uri.getQuery())) {
				Map<String, Object> params = createGetParamMap(uri.getQuery());
				setParams(params);
			}
			if (params == null)
				params = new HashMap<>();
			if (hr instanceof FullHttpRequest) {
				FullHttpRequest request = (FullHttpRequest) hr;
				setContent(request.content().toString(charset));
				String contentTypeValue = hr.headers().get("Content-Type");
				
				if(!StringUtil.isNullOrEmpty(contentTypeValue)){
					if(contentTypeValue.contains("application/json")){
						return;
					}
				}
				HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
				List<InterfaceHttpData> postData = decoder.getBodyHttpDatas(); //
				for (InterfaceHttpData data : postData) {
					if (data.getHttpDataType() == HttpDataType.Attribute) {
						MemoryAttribute attribute = (MemoryAttribute) data;
						String value = attribute.getValue();
						params.put(attribute.getName(), value);
					}
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 解析参数
	 * 
	 * @param query
	 * @return
	 */
	public Map<String, Object> createGetParamMap(String query) {
		int index = -1;
		if ((index = query.indexOf(";")) != -1) {
			String cookieStr = query.substring(index + 1);
			Set<Cookie> cookiesSet = ServerCookieDecoder.LAX.decode(cookieStr);
			if (cookiesSet != null && cookiesSet.size() > 0) {
				Iterator<Cookie> it = cookiesSet.iterator();
				while (it.hasNext()) {
					Cookie cookie = it.next();
					cookies.put(cookie.name(), cookie);
				}
			}
			query = query.substring(0, index);
		}

		Map<String, Object> params = new HashMap<String, Object>();
		String[] querys = query.split("&");
		for (int i = 0; i < querys.length; i++) {
			String paramQuery = querys[i];
			String[] map = paramQuery.split("=", 2);
			if (map == null || map.length != 2)
				continue;
			params.put(map[0], map[1]);
		}
		return params;
	}

	@Override
	public String toString() {
		return "HttpRequestMessage [url=" + url + "]; method=" + this.method().name();
	}

}
