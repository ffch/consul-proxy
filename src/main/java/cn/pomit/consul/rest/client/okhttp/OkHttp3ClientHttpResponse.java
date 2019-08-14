/*
 * Copyright 2002-2017 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.pomit.consul.rest.client.ClientHttpResponse;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * {@link ClientHttpResponse} implementation based on OkHttp 3.x.
 *
 * @author wuguangkuo
 *
 */
public final class OkHttp3ClientHttpResponse implements ClientHttpResponse {

	private final Response response;

	public OkHttp3ClientHttpResponse(Response response) {
		this.response = response;
	}

	@Override
	public int getStatusCode() {
		return this.response.code();
	}

	@Override
	public InputStream getBody() {
		ResponseBody body = this.response.body();
		return (body != null ? body.byteStream() : new ByteArrayInputStream(new byte[0]));
	}

	@Override
	public void close() {
		ResponseBody body = this.response.body();
		if (body != null) {
			body.close();
		}
	}

	@Override
	public String getBodyStr() throws IOException {
		ResponseBody body = this.response.body();
		if (body != null) {
			String bodyStr = body.string();
			body.close();
			return bodyStr;
		}
		return null;
	}

	@Override
	public String getHeader(String name) {
		return response.header(name);
	}
}
