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

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import cn.pomit.consul.rest.client.ClientHttpResponse;

/**
 * {@link ClientHttpResponse} implementation based on Apache HttpComponents
 * HttpClient.
 *
 * <p>
 * Created via the {@link HttpComponentsClientHttpRequest}.
 *
 * @author wuguangkuo
 *
 */
public final class HttpComponentsClientHttpResponse implements ClientHttpResponse {

	private final HttpResponse httpResponse;
	private Boolean closed = false;

	@Override
	public int getStatusCode() {
		return httpResponse.getStatusLine().getStatusCode();
	}

	public HttpComponentsClientHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	@Override
	public InputStream getBody() throws IOException {
		HttpEntity entity = this.httpResponse.getEntity();
		return (entity != null ? entity.getContent() : new ByteArrayInputStream(new byte[0]));
	}

	@Override
	public void close() {
		if (!closed) {
			// Release underlying connection back to the connection manager
			try {
				try {
					// Attempt to keep connection alive by consuming its
					// remaining content
					EntityUtils.consume(this.httpResponse.getEntity());
				} finally {
					if (this.httpResponse instanceof Closeable) {
						((Closeable) this.httpResponse).close();
					}
				}
			} catch (IOException ex) {
				// Ignore exception on close...
			}
		}
		closed = true;
	}

	@Override
	public String getBodyStr() throws IOException {
		HttpEntity entity = this.httpResponse.getEntity();
		if (entity != null) {
			String bodyStr = EntityUtils.toString(entity, Consts.UTF_8);
			close();
			return bodyStr;
		}
		return null;
	}

	@Override
	public String getHeader(String name) {
		Header header = httpResponse.getFirstHeader(name);
		return header == null ? null : header.getValue();
	}
}
