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

package cn.pomit.consul.rest.client.okhttp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import cn.pomit.consul.rest.client.AbstractClientHttpRequest;
import cn.pomit.consul.rest.client.ClientHttpRequest;
import cn.pomit.consul.rest.client.ClientHttpResponse;
import cn.pomit.consul.rest.client.function.BiFunction;
import cn.pomit.consul.rest.client.function.Function;
import cn.pomit.consul.rest.client.function.TriFunction;
import cn.pomit.consul.util.NameValuePair;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * {@link ClientHttpRequest} implementation based on OkHttp 3.x.
 *
 * @author wuguangkuo
 *
 */
public final class OkHttp3ClientHttpRequest
		extends AbstractClientHttpRequest<HttpUrl, Request.Builder, RequestBody, Request> implements ClientHttpRequest {
	private final OkHttpClient client;
	private static final String HTTP_GET = "GET";
	private Charset charset = StandardCharsets.UTF_8;
	private final MediaType APPLICATION_JSON;

	private BiFunction queryParamFunc = new BiFunction<String, List<NameValuePair>, HttpUrl>() {
		@Override
		public HttpUrl apply(String url, List<NameValuePair> params) {
			HttpUrl.Builder requestUrlBuilder = HttpUrl.parse(url).newBuilder();
			if (params != null && !params.isEmpty()) {
				for (NameValuePair nameValuePair : params) {
					if (nameValuePair.getValue() != null) {
						requestUrlBuilder.addQueryParameter(nameValuePair.getName(), nameValuePair.getValue());
					}
				}
			}
			return requestUrlBuilder.build();
		}
	};

	private BiFunction urlFunc = new BiFunction<String, Object, HttpUrl>() {
		@Override
		public HttpUrl apply(String url, Object params) {
			return HttpUrl.parse(url);
		}
	};

	private Function<List<NameValuePair>, RequestBody> formBodyFunc = new Function<List<NameValuePair>, RequestBody>() {
		@Override
		public RequestBody apply(List<NameValuePair> nameValuePairs) {
			return getFormBody(nameValuePairs);
		}
	};

	private Function<List<NameValuePair>, RequestBody> multipartBodyFunc = new Function<List<NameValuePair>, RequestBody>() {
		@Override
		public RequestBody apply(List<NameValuePair> nameValuePairs) {
			return getMultipartBody(nameValuePairs);
		}
	};

	private Function<String, RequestBody> textBodyFunc = new Function<String, RequestBody>() {
		@Override
		public RequestBody apply(String jsonText) {
			return getTextBody(jsonText);
		}
	};

	private TriFunction<HttpUrl, String, RequestBody, Request.Builder> requestBuilderFunc = new TriFunction<HttpUrl, String, RequestBody, Request.Builder>() {
		@Override
		public Request.Builder apply(HttpUrl url, String method, RequestBody body) {
			return new Request.Builder().url(url).method(method, body);
		}
	};

	private TriFunction<String, String, Request.Builder, Void> headerAction = new TriFunction<String, String, Request.Builder, Void>() {
		@Override
		public Void apply(String name, String value, Request.Builder builder) {
			builder.addHeader(name, value);
			return null;
		}
	};

	private Function<Request.Builder, Request> requestFunc = new Function<Request.Builder, Request>() {
		@Override
		public Request apply(Request.Builder builder) {
			return builder.build();
		}
	};

	public OkHttp3ClientHttpRequest(OkHttpClient client) {
		this.client = client;
		APPLICATION_JSON = MediaType.parse("application/json;charset=UTF-8");
	}

	@Override
	public ClientHttpResponse executeGetRequest(String baseUrl, String path, List<NameValuePair> paramList,
			Map<String, String> headerMap, Map<String, String> cookieMap) throws IOException {
		return executeRequest(baseUrl, path, HTTP_GET, paramList, queryParamFunc, null, requestBuilderFunc, headerMap,
				cookieMap, requestFunc, headerAction);
	}

	@Override
	public ClientHttpResponse executeCommonFormRequest(String baseUrl, String path, final String httpMethod,
			List<NameValuePair> paramList, Map<String, String> headerMap, Map<String, String> cookieMap)
			throws IOException {
		return executeRequest(baseUrl, path, httpMethod, paramList, urlFunc, formBodyFunc, requestBuilderFunc,
				headerMap, cookieMap, requestFunc, headerAction);
	}

	@Override
	public ClientHttpResponse executeMultipartFormRequest(String baseUrl, String path, final String httpMethod,
			List<NameValuePair> paramList, Map<String, String> headerMap, Map<String, String> cookieMap)
			throws IOException {
		return executeRequest(baseUrl, path, httpMethod, paramList, urlFunc, multipartBodyFunc, requestBuilderFunc,
				headerMap, cookieMap, requestFunc, headerAction);
	}

	@Override
	public ClientHttpResponse executeTextBodyRequest(String baseUrl, String path, final String httpMethod,
			String jsonBody, Map<String, String> headerMap, Map<String, String> cookieMap) throws IOException {
		return executeRequest(baseUrl, path, httpMethod, jsonBody, urlFunc, textBodyFunc, requestBuilderFunc, headerMap,
				cookieMap, requestFunc, headerAction);
	}

	private RequestBody getFormBody(List<NameValuePair> paramList) {
		FormBody.Builder formBuilder = new FormBody.Builder(charset);
		if (paramList != null && !paramList.isEmpty()) {
			for (NameValuePair entry : paramList) {
				if (entry.getValue() != null) {
					formBuilder.add(entry.getName(), entry.getValue());
				}
			}
		}
		return formBuilder.build();
	}

	private RequestBody getTextBody(String bodyText) {
		String validText = bodyText == null ? "" : bodyText;
		return RequestBody.create(APPLICATION_JSON, validText);
	}

	private RequestBody getMultipartBody(List<NameValuePair> paramList) {
		MultipartBody.Builder builder = new MultipartBody.Builder();
		builder.setType(MultipartBody.FORM);
		if (paramList != null && !paramList.isEmpty()) {
			for (NameValuePair entry : paramList) {
				if (entry.getValue() != null) {
					builder.addFormDataPart(entry.getName(), entry.getValue());
				}
			}
		}
		return builder.build();
	}

	@Override
	protected byte[] getBodyBytes(RequestBody requestBody) throws IOException {
		if (requestBody == null) {
			return null;
		}
		Buffer buffer = new Buffer();
		requestBody.writeTo(buffer);
		byte[] bodyBytes = buffer.readByteArray();
		buffer.flush();
		buffer.close();
		return bodyBytes;
	}

	@Override
	protected ClientHttpResponse execute(Request request) throws IOException {
		return new OkHttp3ClientHttpResponse(this.client.newCall(request).execute());
	}
}
