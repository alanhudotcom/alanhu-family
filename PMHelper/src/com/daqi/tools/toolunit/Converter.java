package com.daqi.tools.toolunit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Converter {
	
	public static String suppoetTozh(String str) {
		String result = str;
		try {
			System.out.println("encoding = " + System.getProperty("file.encoding"));
			//转换成中文支持
			result = new String(str.getBytes("ISO-8859-1"), System.getProperty("file.encoding"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public static String encryptBASE64(String value) {
		String result = value;
		if (value == null || value.startsWith("encode_")) {
			//证明已经加密，不需再加密
			Logger.println("not need to encrypt");
			return result;
		}
		
		try {
			String tmp = new BASE64Encoder().encodeBuffer(value.getBytes("ISO-8859-1"));
			result = "encode_" + tmp; 
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result; 
	}
	
	public static String decryptBASE64(String value) {
		String result = value;
		if (value == null || !value.startsWith("encode_")) {
			//证明已经解密，不需再解密
			Logger.println("not need to decrypt");
			return result;
		}
		try {
			String realValue = value.substring(7);
			byte[] tmp = new BASE64Decoder().decodeBuffer(realValue);
			result = new String(tmp, "ISO-8859-1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result; 
	}

}
