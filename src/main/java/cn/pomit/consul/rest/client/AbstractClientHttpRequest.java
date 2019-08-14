package cn.pomit.consul.rest.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.pomit.consul.rest.client.function.BiFunction;
import cn.pomit.consul.rest.client.function.Function;
import cn.pomit.consul.rest.client.function.TriFunction;
import cn.pomit.consul.util.InetUtil;
import cn.pomit.consul.util.URLUtil;

/**
 * @param <L>
 *            URL类型
 * @param <S>
 *            request builder类型
 * @param <R>
 *            request body类型
 * @param <U>
 *            request类型
 * @author wuguangkuo
 * @create 2018-08-23 16:57
 **/
public abstract class AbstractClientHttpRequest<L, S, R, U> implements ClientHttpRequest {
	private String localAddressHeader = "x-local-addr";
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private void addHeaders(Map<String, String> headerMap, S requestBuilder,
			TriFunction<String, String, S, Void> action) {
		if (headerMap != null && !headerMap.isEmpty()) {
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				String headerName = entry.getKey();
				String headerValue = entry.getValue();
				action.apply(headerName, headerValue, requestBuilder);
			}
		}
	}

	private void addCookieHeader(Map<String, String> cookieMap, S requestBuilder,
			TriFunction<String, String, S, Void> action) {
		if (cookieMap != null && !cookieMap.isEmpty()) {
			StringBuilder cookieValueStr = new StringBuilder();
			for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
				String cookieName = entry.getKey();
				String cookieValue = entry.getValue();
				cookieValueStr.append(cookieName).append("=").append(cookieValue).append("; ");
			}

			// 删除最后一个元素后边的分号和空格符
			cookieValueStr.deleteCharAt(cookieValueStr.length() - 1).deleteCharAt(cookieValueStr.length() - 1);
			action.apply("Cookie", cookieValueStr.toString(), requestBuilder);
		}
	}

	protected <T> ClientHttpResponse executeRequest(String baseUrl, String path, String method, T requestParam,
			BiFunction<String, T, L> urlFunc, Function<T, R> bodyFunc, TriFunction<L, String, R, S> requestBuilderFunc,
			Map<String, String> headerMap, Map<String, String> cookieMap, Function<S, U> requestFunc,
			TriFunction<String, String, S, Void> headerAction) throws IOException {
		R requestBody = null;
		if (bodyFunc != null) {
			requestBody = bodyFunc.apply(requestParam);
		}
		String urlStr = baseUrl + URLUtil.completePath(path);
		L url = urlFunc.apply(urlStr, requestParam);
		S requestBuilder = requestBuilderFunc.apply(url, method, requestBody);
		Map<String, String> allHeaderMap = new HashMap<>(4);
		if (headerMap != null) {
			allHeaderMap.putAll(headerMap);
		}
		allHeaderMap.put(localAddressHeader, InetUtil.findFirstNonLoopbackAddress(null).getHostAddress());
		addHeaders(allHeaderMap, requestBuilder, headerAction);
		if (cookieMap == null) {
			cookieMap = new HashMap<>(0);
		}
		addCookieHeader(cookieMap, requestBuilder, headerAction);
		U request = requestFunc.apply(requestBuilder);

		ClientHttpResponse response = null;
		try {
			response = execute(request);
			int statusCode = response.getStatusCode();
			if (statusCode < 200 || statusCode >= 300) {
				logger.error("调用失败，常见HTTP响应码含义请参考文档：http://192.168.2.11:4000/api-gateway/section8/questionList.html");
			}
		} catch (Throwable t) {
			logger.error("请求异常：", t);

			throw t;
		}
		return response;
	}

	/**
	 * 获取request body的字节数组
	 * 
	 * @param r
	 *            request body参数
	 * @throws IOException
	 * @return
	 */
	protected abstract byte[] getBodyBytes(R r) throws IOException;

	/**
	 * 执行请求
	 * 
	 * @param u
	 *            Request对象
	 * @throws IOException
	 * @return
	 */
	protected abstract ClientHttpResponse execute(U u) throws IOException;

}
