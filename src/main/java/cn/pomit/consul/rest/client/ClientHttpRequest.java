/*
 * Copyright 2002-2014 the original author or authors.
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

package cn.pomit.consul.rest.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.pomit.consul.util.NameValuePair;

/**
 * Represents a client-side HTTP request.
 *
 * receiving a {@link ClientHttpResponse} which can be read from.
 *
 * @author wuguangkuo
 *
 */
public interface ClientHttpRequest {
	/**
	 * GET请求
	 * 
	 * @param baseUrl
	 * @param path
	 * @param paramList
	 * @param headerMap
	 * @param cookieMap
	 * @return
	 * @throws IOException
	 */
	ClientHttpResponse executeGetRequest(String baseUrl, String path, List<NameValuePair> paramList,
			Map<String, String> headerMap, Map<String, String> cookieMap) throws IOException;

	/**
	 * 普通表单请求
	 * 
	 * @param baseUrl
	 * @param path
	 * @param httpMethod
	 * @param paramList
	 * @param headerMap
	 * @param cookieMap
	 * @return
	 * @throws IOException
	 */
	ClientHttpResponse executeCommonFormRequest(String baseUrl, String path, String httpMethod,
			List<NameValuePair> paramList, Map<String, String> headerMap, Map<String, String> cookieMap)
			throws IOException;

	/**
	 * multipart-form请求
	 * 
	 * @param baseUrl
	 * @param path
	 * @param httpMethod
	 * @param paramList
	 * @param headerMap
	 * @param cookieMap
	 * @return
	 * @throws IOException
	 */
	ClientHttpResponse executeMultipartFormRequest(String baseUrl, String path, String httpMethod,
			List<NameValuePair> paramList, Map<String, String> headerMap, Map<String, String> cookieMap)
			throws IOException;

	/**
	 * json body请求
	 * 
	 * @param baseUrl
	 * @param path
	 * @param httpMethod
	 * @param jsonBody
	 * @param headerMap
	 * @param cookieMap
	 * @return
	 * @throws IOException
	 */
	ClientHttpResponse executeTextBodyRequest(String baseUrl, String path, String httpMethod, String jsonBody,
			Map<String, String> headerMap, Map<String, String> cookieMap) throws IOException;
}
