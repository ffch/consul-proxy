package cn.pomit.consul.rest;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.loadbalancer.Server;

import cn.pomit.consul.discovery.ConsulDiscovery;
import cn.pomit.consul.rest.client.ClientHttpRequest;
import cn.pomit.consul.rest.client.ClientHttpResponse;
import cn.pomit.consul.rest.client.okhttp.OkHttp3ClientHttpRequest;
import cn.pomit.consul.util.NameValuePair;
import cn.pomit.consul.util.URLUtil;
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

	public static void initConfiguration(ConsulDiscovery consulDiscovery) {
		instance = new RestClient();
		if (instance.clientHttpRequest == null) {
			instance.clientHttpRequest = new OkHttp3ClientHttpRequest(new OkHttpClient.Builder().build());
		}
		instance.consulDiscovery = consulDiscovery;
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
		return baseUrl;
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
