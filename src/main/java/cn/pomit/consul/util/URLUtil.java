package cn.pomit.consul.util;

/**
 * URL工具类
 *
 * @author wuguangkuo
 **/
public class URLUtil {
	public static final String URI_SEPARATOR = "/";
	public static final String URI_SCHEME_SEPARATOR = "://";

	/**
	 * 删除baseUrl结尾的"/"
	 * 
	 * @param baseUrl url地址
	 * @return 修改后的url
	 */
	public static String trimBaseUrl(String baseUrl) {
		String trimUrl = baseUrl.trim();
		if (trimUrl.endsWith(URI_SEPARATOR)) {
			trimUrl = trimUrl.substring(0, trimUrl.length() - 1);
		}

		return trimUrl;
	}

	/**
	 * 给指定的path开头拼接上缺少的/
	 * 
	 * @param path url地址
	 * @return 修改后的url
	 */
	public static String completePath(String path) {
		String trimPath = path.trim();
		if (!trimPath.startsWith(URI_SEPARATOR)) {
			trimPath = URI_SEPARATOR + trimPath;
		}
		return trimPath;
	}

	/**
	 * 给定一个普通url,如：https://www.baidu.com/，解析其host部分
	 * 
	 * @param url url地址
	 * @return 修改后的url
	 */
	public static String parseHost(String url) {
		int beginIdx = 0;
		String https = "https://";
		String http = "http://";
		if (url.regionMatches(true, beginIdx, https, 0, https.length())) {
			beginIdx += https.length();
		} else if (url.regionMatches(true, beginIdx, http, 0, http.length())) {
			beginIdx += http.length();
		}

		if (beginIdx == 0) {
			return null;
		}

		int endIdx;
		char slash = '/';
		if ((endIdx = url.indexOf(slash, beginIdx)) == -1) {
			return url.substring(beginIdx);
		} else {
			return url.substring(beginIdx, endIdx);
		}
	}
}
