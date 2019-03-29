package cn.pomit.consul.http;

import java.util.List;

import cn.pomit.consul.http.res.ResCode;
import cn.pomit.consul.http.res.ResType;
import io.netty.handler.codec.http.HttpHeaders;

public class HttpResponseMessage {

	public int resCode;

	public String resType;

	public String message = "";

	public String redirectUrl;

	private List<String> encodedCookie;

	private boolean responseNow = false;

	private List<HttpHeaders> headers = null;

	public int getResCode() {
		return resCode;
	}

	public void setResCode(int resCode) {
		this.resCode = resCode;
	}

	public List<HttpHeaders> getHeaders() {
		return headers;
	}

	public void setHeaders(List<HttpHeaders> headers) {
		this.headers = headers;
	}

	public void setNotFound(String url) {
		setResCode(ResCode.NOT_FOUND.getValue());
		setResType(ResType.HTML.getValue());
		setRedirectUrl(url);
		setMessage("找不到该页面。");
		setResponseNow(true);
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public String getMessage() {
		return message;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getEncodedCookie() {
		return encodedCookie;
	}

	public void setEncodedCookie(List<String> encodedCookie) {
		this.encodedCookie = encodedCookie;
	}

	public boolean isResponseNow() {
		return responseNow;
	}

	public void setResponseNow(boolean responseNow) {
		this.responseNow = responseNow;
	}

	@Override
	public String toString() {
		return "HttpResponseMessage [resCode=" + resCode + ", resType=" + resType + ", message=" + message + "]";
	}

}
