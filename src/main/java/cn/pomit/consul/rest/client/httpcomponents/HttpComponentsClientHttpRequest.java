/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.pomit.consul.rest.client.httpcomponents;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;

import cn.pomit.consul.rest.client.AbstractClientHttpRequest;
import cn.pomit.consul.rest.client.ClientHttpRequest;
import cn.pomit.consul.rest.client.ClientHttpResponse;
import cn.pomit.consul.rest.client.function.BiFunction;
import cn.pomit.consul.rest.client.function.Function;
import cn.pomit.consul.rest.client.function.TriFunction;
import cn.pomit.consul.util.NameValuePair;

/**
 * {@link ClientHttpRequest} implementation based on Apache HttpComponents
 * HttpClient.
 *
 *
 * @author wuguangkuo
 */
public final class HttpComponentsClientHttpRequest extends
		AbstractClientHttpRequest<URI, RequestBuilder, HttpEntity, HttpUriRequest> implements ClientHttpRequest {
	private static final String HTTP_GET = "GET";
	private final HttpClient httpClient;
	private static final int READ_BYTES_PER_TIME = 1024;
	private Charset charset = StandardCharsets.UTF_8;
	private final ContentType APPLICATION_JSON;
	private final ContentType TEXT_PLAIN;

	private BiFunction<String, List<NameValuePair>, URI> queryParamFunc = new BiFunction<String, List<NameValuePair>, URI>() {
		@Override
		public URI apply(String url, List<NameValuePair> params) {
			URI uri;
			try {
				URIBuilder uriBuilder = new URIBuilder(url).setCharset(charset);
				if (params != null && !params.isEmpty()) {
					for (NameValuePair entry : params) {
						if (entry.getValue() != null) {
							uriBuilder.addParameter(entry.getName(), entry.getValue());
						}
					}
				}
				uri = uriBuilder.build();
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("URL解析出错:" + url, e);
			}

			return uri;
		}
	};

	private BiFunction urlFunc = new BiFunction<String, Object, URI>() {
		@Override
		public URI apply(String url, Object params) {
			URI uri;
			try {
				uri = new URIBuilder(url).setCharset(charset).build();
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("URL解析出错,url:" + url, e);
			}
			return uri;
		}
	};

	private Function<List<NameValuePair>, HttpEntity> formBodyFunc = new Function<List<NameValuePair>, HttpEntity>() {
		@Override
		public HttpEntity apply(List<NameValuePair> nameValuePairs) {
			return getFormEntity(nameValuePairs);
		}
	};

	private Function<List<NameValuePair>, HttpEntity> multipartBodyFunc = new Function<List<NameValuePair>, HttpEntity>() {
		@Override
		public HttpEntity apply(List<NameValuePair> nameValuePairs) {
			return getMultipartEntity(nameValuePairs);
		}
	};

	private Function<String, HttpEntity> textBodyFunc = new Function<String, HttpEntity>() {
		@Override
		public HttpEntity apply(String jsonText) {
			return getTextEntity(jsonText);
		}
	};

	private TriFunction<URI, String, HttpEntity, RequestBuilder> requestBuilderFunc = new TriFunction<URI, String, HttpEntity, RequestBuilder>() {
		@Override
		public RequestBuilder apply(URI url, String method, HttpEntity entity) {
			URI uri;
			try {
				uri = new URIBuilder(url).setCharset(charset).build();
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("URL解析出错:" + url, e);
			}
			return RequestBuilder.create(method).setCharset(charset).setUri(uri).setEntity(entity);
		}
	};

	private Function<RequestBuilder, HttpUriRequest> requestFunc = new Function<RequestBuilder, HttpUriRequest>() {
		@Override
		public HttpUriRequest apply(RequestBuilder requestBuilder) {
			return requestBuilder.build();
		}
	};

	private TriFunction<String, String, RequestBuilder, Void> headerAction = new TriFunction<String, String, RequestBuilder, Void>() {
		@Override
		public Void apply(String name, String value, RequestBuilder requestBuilder) {
			requestBuilder.addHeader(name, value);
			return null;
		}
	};

	/**
	 *
	 * @param client
	 */
	public HttpComponentsClientHttpRequest(HttpClient client) {
		this.httpClient = client;

		APPLICATION_JSON = ContentType.create("application/json", charset);
		TEXT_PLAIN = ContentType.create("text/plain", charset);
	}

	@Override
	public ClientHttpResponse executeGetRequest(String baseUrl, String path, List<NameValuePair> paramList,
			Map<String, String> headerMap, Map<String, String> cookieMap) throws IOException {
		return executeRequest(baseUrl, path, HTTP_GET, paramList, queryParamFunc, null, requestBuilderFunc, headerMap,
				cookieMap, requestFunc, headerAction);
	}

	@Override
	public ClientHttpResponse executeCommonFormRequest(String baseUrl, String path, String httpMethod,
			List<NameValuePair> paramList, Map<String, String> headerMap, Map<String, String> cookieMap)
			throws IOException {
		return executeRequest(baseUrl, path, httpMethod, paramList, urlFunc, formBodyFunc, requestBuilderFunc,
				headerMap, cookieMap, requestFunc, headerAction);
	}

	@Override
	public ClientHttpResponse executeMultipartFormRequest(String baseUrl, String path, String httpMethod,
			List<NameValuePair> paramList, Map<String, String> headerMap, Map<String, String> cookieMap)
			throws IOException {
		return executeRequest(baseUrl, path, httpMethod, paramList, urlFunc, multipartBodyFunc, requestBuilderFunc,
				headerMap, cookieMap, requestFunc, headerAction);
	}

	@Override
	public ClientHttpResponse executeTextBodyRequest(String baseUrl, String path, String httpMethod, String jsonBody,
			Map<String, String> headerMap, Map<String, String> cookieMap) throws IOException {
		return executeRequest(baseUrl, path, httpMethod, jsonBody, urlFunc, textBodyFunc, requestBuilderFunc, headerMap,
				cookieMap, requestFunc, headerAction);
	}

	private HttpEntity getTextEntity(String bodyText) {
		return EntityBuilder.create().setContentType(APPLICATION_JSON).setText(bodyText).build();
	}

	private HttpEntity getMultipartEntity(List<NameValuePair> paramList) {

		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setCharset(charset);

		if (paramList != null && !paramList.isEmpty()) {
			for (NameValuePair entry : paramList) {
				if (entry.getValue() != null) {
					entityBuilder.addTextBody(entry.getName(), entry.getValue(), TEXT_PLAIN);
				}
			}
		}

		return entityBuilder.build();
	}

	private HttpEntity getFormEntity(List<NameValuePair> paramList) {
		List<org.apache.http.NameValuePair> formParams = new ArrayList<>();
		if (paramList != null && !paramList.isEmpty()) {
			for (NameValuePair entry : paramList) {
				if (entry.getValue() != null) {
					formParams.add(new BasicNameValuePair(entry.getName(), entry.getValue()));
				}
			}
		}
		return new UrlEncodedFormEntity(formParams, charset);
	}

	@Override
	protected byte[] getBodyBytes(HttpEntity httpEntity) throws IOException {
		if (httpEntity == null) {
			return null;
		}
		BufferedInputStream inputStream = new BufferedInputStream(httpEntity.getContent());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(READ_BYTES_PER_TIME);
		byte[] readBytes = new byte[READ_BYTES_PER_TIME];
		int count;
		while ((count = inputStream.read(readBytes)) != -1) {
			outputStream.write(readBytes, 0, count);
		}
		byte[] bodyBytes = outputStream.toByteArray();
		outputStream.close();
		inputStream.close();
		return bodyBytes;
	}

	@Override
	protected ClientHttpResponse execute(HttpUriRequest httpUriRequest) throws IOException {
		return new HttpComponentsClientHttpResponse(httpClient.execute(httpUriRequest));
	}
}
