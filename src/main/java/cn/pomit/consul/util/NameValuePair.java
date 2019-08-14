package cn.pomit.consul.util;

/**
 * @author wuguangkuo
 * @create 2018-08-23 19:13
 **/
public class NameValuePair {
	private String name;
	private String value;

	public NameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "NameValuePair{" + "name='" + name + '\'' + ", value='" + value + '\'' + '}';
	}
}
