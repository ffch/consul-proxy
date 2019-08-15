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

package cn.pomit.consul.rest.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a client-side HTTP response.
 *
 * <p>
 * A {@code ClientHttpResponse} must be {@linkplain #close() closed}, typically
 * in a {@code finally} block.
 *
 * @author wuguangkuo
 *
 */
public interface ClientHttpResponse extends Closeable {

	/**
	 * Return the HTTP status code of the response.
	 * 
	 * @return the HTTP status as an HttpStatus enum value
	 */
	int getStatusCode();

	/**
	 * Return the body of the message as an input stream.
	 * 
	 * @return the input stream body (never {@code null})
	 * @throws IOException
	 *             in case of I/O Errors
	 */
	InputStream getBody() throws IOException;

	/**
	 * Close this response, freeing any resources created.
	 */
	@Override
	void close();

	/**
	 * 获取body字符串，完成之后关闭流
	 * 
	 * @return 报文体
	 * @throws IOException io异常
	 */
	String getBodyStr() throws IOException;

	/**
	 * 获取响应header
	 * 
	 * @param name
	 *            header名称
	 * @return 响应header
	 */
	String getHeader(String name);
}
