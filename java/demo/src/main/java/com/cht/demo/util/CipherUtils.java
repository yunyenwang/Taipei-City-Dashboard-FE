package com.cht.demo.util;

import org.springframework.util.DigestUtils;

public class CipherUtils {

	public static String md5(String text) {
		return  DigestUtils.md5DigestAsHex(text.getBytes());
	}
}
