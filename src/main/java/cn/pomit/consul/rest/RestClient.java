package cn.pomit.consul.rest;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.netflix.loadbalancer.Server;

import cn.pomit.consul.discovery.ConsulDiscovery;
import cn.pomit.consul.rest.client.ClientHttpRequest;
import cn.pomit.consul.rest.client.ClientHttpResponse;
import cn.pomit.consul.rest.client.httpcomponents.HttpComponentsClientHttpRequest;
import cn.pomit.consul.rest.client.okhttp.OkHttp3ClientHttpRequest;
import cn.pomit.consul.util.NameValuePair;
import cn.pomit.consul.util.URLUtil;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * kong api client
 *
 * @author wuguangkuo
 * @since 1.6-SNAPSHOT 2018-08-23
 **/
public class RestClient {
	private static RestClient instance = null;
	private ClientHttpRequest clientHttpRequest;
	private ConsulDiscovery consulDiscovery;
	private static final String HTTP_POST = "POST";
	private static final String HTTP_PUT = "PUT";
	private static final String HTTP_DELETE = "DELETE";
	private static final String HTTP_PATCH = "PATCH";
	private static Logger logger = LoggerFactory.getLogger(RestClient.class);

	private RestClient() {

	}

	public static void initConfiguration(ConsulDiscovery consulDiscovery, RestClientConfig restClientConfig) {
		instance = new RestClient();
		instance.consulDiscovery = consulDiscovery;

		if (restClientConfig.getHttpType().equalsIgnoreCase(RestClientConfig.OKTTP_TYPE)) {
			logger.info("使用okHttp作为Rest请求客户端");
			OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
			okHttpClientBuilder.connectTimeout(restClientConfig.getTimeout(), TimeUnit.MILLISECONDS)
					.readTimeout(restClientConfig.getTimeout(), TimeUnit.MILLISECONDS)
					.writeTimeout(restClientConfig.getTimeout(), TimeUnit.MILLISECONDS);
			if (restClientConfig.isRetry()) {
				okHttpClientBuilder.retryOnConnectionFailure(true);
			} else {
				okHttpClientBuilder.retryOnConnectionFailure(false);
			}
			if (restClientConfig.isEnablePool()) {
				ConnectionPool connectionPool = new ConnectionPool(restClientConfig.getPoolMaxIdle(), 10,
						TimeUnit.MINUTES);
				okHttpClientBuilder.connectionPool(connectionPool);
			}
			OkHttpClient okHttpClient = okHttpClientBuilder.build();
			ClientHttpRequest httpClientRequest = new OkHttp3ClientHttpRequest(okHttpClient);
			instance.clientHttpRequest = httpClientRequest;
		} else if (restClientConfig.getHttpType().equalsIgnoreCase(RestClientConfig.HTTPCLIENT_TYPE)) {
			logger.info("使用HttpClient作为Rest请求客户端");
			SocketConfig socketConfig = SocketConfig.custom().setSoReuseAddress(true)
					.setSoTimeout(restClientConfig.getTimeout()).setSoKeepAlive(true).build();

			HttpRequestRetryHandler requestRetryHandler = new DefaultHttpRequestRetryHandler(0, false);

			if (restClientConfig.isRetry()) {
				requestRetryHandler = new DefaultHttpRequestRetryHandler(restClientConfig.getRetryTimes(), true);
			}
			if (restClientConfig.isEnablePool()) {
				PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager();
				clientConnectionManager.setMaxTotal(restClientConfig.getPoolMaxIdle());
				clientConnectionManager.setDefaultSocketConfig(socketConfig);
				CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(clientConnectionManager)
						.setRetryHandler(requestRetryHandler).build();
				instance.clientHttpRequest = new HttpComponentsClientHttpRequest(httpClient);
			} else {
				instance.clientHttpRequest = new HttpComponentsClientHttpRequest(HttpClientBuilder.create()
						.setDefaultSocketConfig(socketConfig).setRetryHandler(requestRetryHandler).build());
			}
		} else {
			logger.info("未设置客户端类型，请手动实现并添加ClientHttpRequest的子类。");
		}
	}

	public static RestClient getInstance() {
		return instance;
	}

	private String getSeverBaseUrl(URI uri) {
		Server server = consulDiscovery.discovery(uri.getHost());
		String baseUrl = uri.getScheme() + URLUtil.URI_SCHEME_SEPARATOR + uri.getHost()
				+ (uri.getPort() == -1 ? "" : (":" + uri.getPort()));
		if (server != null) {
			baseUrl = uri.getScheme() + URLUtil.URI_SCHEME_SEPARATOR + server.getHost()
					+ (server.getPort() == -1 ? "" : (":" + server.getPort()));
		}
		logger.info("请求域为：{}", baseUrl);

		return baseUrl;
	}

	public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) throws IOException {
		List<NameValuePair> paramList = null;
		if (uriVariables != null) {
			paramList = uriVariables.entrySet().stream()
					.map(e -> new NameValuePair(e.getKey(), e.getValue().toString())).collect(Collectors.toList());
		}

		ClientHttpResponse response = get(url, paramList);
		String responseBodyStr = (response == null) ? null : response.getBodyStr();

		if (responseBodyStr == null || responseBodyStr == null)
			return null;
		return JSONObject.parseObject(responseBodyStr, responseType);
	}

	public <T> T postForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) throws IOException {
		List<NameValuePair> paramList = null;
		if (uriVariables != null) {
			paramList = uriVariables.entrySet().stream()
					.map(e -> new NameValuePair(e.getKey(), e.getValue().toString())).collect(Collectors.toList());
		}
		ClientHttpResponse response = postForm(url, paramList);
		String responseBodyStr = (response == null) ? null : response.getBodyStr();
		if (responseBodyStr == null || responseBodyStr == null)
			return null;
		return JSONObject.parseObject(responseBodyStr, responseType);
	}

	public <E, T> T postForObject(String url, Class<T> responseType, E object) throws IOException {
		ClientHttpResponse response = postTextBody(url, JSONObject.toJSONString(object));
		String responseBodyStr = (response == null) ? null : response.getBodyStr();
		if (responseBodyStr == null || responseBodyStr == null)
			return null;
		return JSONObject.parseObject(responseBodyStr, responseType);
	}

	/**
	 * 发送get请求
	 *
	 * @param path
	 *            请求path
	 * @param paramList
	 *            请求参数
	 * @return
	 */
	public ClientHttpResponse get(String url, List<NameValuePair> paramList) throws IOException {
		return get(url, paramList, null, null);
	}

	/**
	 * 发送get请求
	 *
	 * @param path
	 *            请求path
	 * @param paramList
	 *            请求参数
	 * @return
	 */
	public ClientHttpResponse get(String url, List<NameValuePair> paramList, Map<String, String> headerMap,
			Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());

		return clientHttpRequest.executeGetRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()), paramList,
				headerMap, cookieMap);
	}

	/**
	 * post请求
	 *
	 * @param path
	 *            请求path
	 * @return
	 */
	public ClientHttpResponse postForm(String url, List<NameValuePair> paramList) throws IOException {
		return postForm(url, paramList, null, null);
	}

	/**
	 * post请求
	 *
	 * @param path
	 *            请求path
	 * @return
	 */
	public ClientHttpResponse postForm(String url, List<NameValuePair> paramList, Map<String, String> headerMap,
			Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());
		return clientHttpRequest.executeCommonFormRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()),
				HTTP_POST, paramList, headerMap, cookieMap);
	}

	/**
	 * 支持 mutilpart/form-data方式的请求
	 * 
	 * @param path
	 *            请求path
	 * @param paramList
	 *            表单参数
	 * @return
	 * @throws IOException
	 */
	public ClientHttpResponse postMultipartForm(String url, List<NameValuePair> paramList) throws IOException {
		return postMultipartForm(url, paramList, null, null);
	}

	/**
	 * 支持 mutilpart/form-data方式的请求
	 * 
	 * @param path
	 *            请求path
	 * @param paramList
	 *            表单参数
	 * @return
	 * @throws IOException
	 */
	public ClientHttpResponse postMultipartForm(String url, List<NameValuePair> paramList,
			Map<String, String> headerMap, Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());

		return clientHttpRequest.executeMultipartFormRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()),
				HTTP_POST, paramList, headerMap, cookieMap);
	}

	public ClientHttpResponse postTextBody(String url, String jsonText) throws IOException {
		return postTextBody(url, jsonText, null, null);
	}

	public ClientHttpResponse postTextBody(String url, String jsonText, Map<String, String> headerMap,
			Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());
		return clientHttpRequest.executeTextBodyRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()),
				HTTP_POST, jsonText, headerMap, cookieMap);
	}

	public ClientHttpResponse patchForm(String url, List<NameValuePair> paramList) throws IOException {
		return patchForm(url, paramList, null, null);
	}

	public ClientHttpResponse patchForm(String url, List<NameValuePair> paramList, Map<String, String> headerMap,
			Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());
		return clientHttpRequest.executeCommonFormRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()),
				HTTP_PATCH, paramList, headerMap, cookieMap);
	}

	public ClientHttpResponse patchTextBody(String url, String jsonText) throws IOException {
		return patchTextBody(url, jsonText, null, null);
	}

	public ClientHttpResponse patchTextBody(String url, String jsonText, Map<String, String> headerMap,
			Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());
		return clientHttpRequest.executeTextBodyRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()),
				HTTP_PATCH, jsonText, headerMap, cookieMap);
	}

	public ClientHttpResponse putForm(String url, List<NameValuePair> paramList) throws IOException {
		return putForm(url, paramList, null, null);
	}

	public ClientHttpResponse putForm(String url, List<NameValuePair> paramList, Map<String, String> headerMap,
			Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());
		return clientHttpRequest.executeCommonFormRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()),
				HTTP_PUT, paramList, headerMap, cookieMap);
	}

	public ClientHttpResponse putTextBody(String url, String jsonText) throws IOException {
		return putTextBody(url, jsonText, null, null);
	}

	public ClientHttpResponse putTextBody(String url, String jsonText, Map<String, String> headerMap,
			Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());
		return clientHttpRequest.executeTextBodyRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()),
				HTTP_PUT, jsonText, headerMap, cookieMap);
	}

	public ClientHttpResponse deleteForm(String url, List<NameValuePair> paramList) throws IOException {
		return deleteForm(url, paramList, null, null);
	}

	public ClientHttpResponse deleteForm(String url, List<NameValuePair> paramList, Map<String, String> headerMap,
			Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());
		return clientHttpRequest.executeCommonFormRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()),
				HTTP_DELETE, paramList, headerMap, cookieMap);
	}

	public ClientHttpResponse deleteTextBody(String url, String jsonText) throws IOException {
		return deleteTextBody(url, jsonText, null, null);
	}

	public ClientHttpResponse deleteTextBody(String url, String jsonText, Map<String, String> headerMap,
			Map<String, String> cookieMap) throws IOException {
		URI uri = URI.create(url.toString());
		return clientHttpRequest.executeTextBodyRequest(getSeverBaseUrl(uri), URLUtil.completePath(uri.getPath()),
				HTTP_DELETE, jsonText, headerMap, cookieMap);
	}

	public ClientHttpRequest getClientHttpRequest() {
		return clientHttpRequest;
	}

	public void setClientHttpRequest(ClientHttpRequest clientHttpRequest) {
		this.clientHttpRequest = clientHttpRequest;
	}

}
