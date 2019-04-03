package cn.pomit.consul.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil {

	public static Properties getProperties(String fileName) throws IOException {
		InputStream resource = PropertyUtil.class.getClassLoader().getResourceAsStream(fileName);
		if(resource == null){
			throw new IOException(fileName + "文件不存在！");
		}
		Properties properties = new Properties();
		properties.load(resource);
		resource.close();
		return properties;
	}
}
