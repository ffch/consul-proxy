package cn.pomit.consul.util;

import java.util.Collection;

public class ReflectUtil {

	/**
	 * 判断是不是集合的实现类
	 * 
	 * @param type class
	 * @return true集合
	 */
	public static boolean isCollection(Class<?> type) {
		return Collection.class.isAssignableFrom(type);
	}
	/**
	 * 是不是java基础类
	 * 
	 * @param type class
	 * @return true:java基础类
	 */
	public static boolean isJavaClass(Class<?> type) {
		boolean isBaseClass = false;
		if (type.isArray()) {
			isBaseClass = false;
		} else if (type.isPrimitive() || type.getPackage() == null || "java.lang".equals(type.getPackage().getName())
				|| "java.math".equals(type.getPackage().getName()) || "java.sql".equals(type.getPackage().getName())
				|| "java.util".equals(type.getPackage().getName())) {
			isBaseClass = true;
		}
		return isBaseClass;
	}
}
