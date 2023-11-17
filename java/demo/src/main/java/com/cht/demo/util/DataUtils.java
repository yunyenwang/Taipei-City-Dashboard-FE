package com.cht.demo.util;

import org.apache.commons.lang3.ObjectUtils;

public class DataUtils {

	public static Integer getValue(Integer i) {
		return ObjectUtils.defaultIfNull(i, 0);
	}
	
	public static String getValue(String s) {
		return ObjectUtils.defaultIfNull(s, "");
	}
}
