package com.tigerjoys.shark.miai;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.shark.miai.common.util.AESFieldUtils;

import java.io.UnsupportedEncodingException;

/** 
 * ClassName: AESCipher <br/> 
 * date: 2017年5月22日 上午11:37:24 <br/> 
 * 
 * @author mouzhanpeng 
 * @version  
 * @since JDK 1.8.0 
 */
public class TestAESCipher {
	
	//加密方式
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	//
	private static final String IV_STRING = "0102030405060708";
	//默认编码
	private static final String CHARSET = "UTF-8";
	 
	public static String aesEncryptString(String content) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		return aesEncryptString(content,IV_STRING);
	}
	
	public static String aesEncryptString(String content, String key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		byte[] contentBytes = content.getBytes(CHARSET);
		byte[] keyBytes = key.getBytes(CHARSET);
		byte[] encryptedBytes = aesEncryptBytes(contentBytes, keyBytes);
		Encoder encoder = Base64.getEncoder();
	    return encoder.encodeToString(encryptedBytes);
	}

	public static String aesDecryptString(String content) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		return aesDecryptString(content,IV_STRING);
	}
	
	public static String aesDecryptString(String content, String key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		Decoder decoder = Base64.getDecoder();
	    byte[] encryptedBytes = decoder.decode(content);
	    byte[] keyBytes = key.getBytes(CHARSET);
		byte[] decryptedBytes = aesDecryptBytes(encryptedBytes, keyBytes);
	    return new String(decryptedBytes, CHARSET);		
	}
	
	public static byte[] aesEncryptBytes(byte[] contentBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
	    return cipherOperation(contentBytes, IV_STRING.getBytes(CHARSET), Cipher.ENCRYPT_MODE);
	}
	
	public static byte[] aesEncryptBytes(byte[] contentBytes, byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
	    return cipherOperation(contentBytes, keyBytes, Cipher.ENCRYPT_MODE);
	}
	
	public static byte[] aesDecryptBytes(byte[] contentBytes, byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
	    return cipherOperation(contentBytes, keyBytes, Cipher.DECRYPT_MODE);
	}
	
	public static byte[] aesDecryptBytes(byte[] contentBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
	    return cipherOperation(contentBytes, IV_STRING.getBytes(CHARSET), Cipher.DECRYPT_MODE);
	}
	
	private static byte[] cipherOperation(byte[] contentBytes, byte[] keyBytes, int mode) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
		
	    byte[] initParam = IV_STRING.getBytes(CHARSET);
	    IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);

	    Cipher cipher = Cipher.getInstance(ALGORITHM);
	    cipher.init(mode, secretKey, ivParameterSpec);

 	 	return cipher.doFinal(contentBytes);
	}
	
	public static void main(String[] args) throws Exception {
		//加密测试----->将对应的头数据加密成密文
		//String code = "{\"word\":\"U\"}";
		String code = "{\"clientId\":\"\",\"version\":\"3.6.0\",\"mobile_brand\":\"x86_64\",\"channel\":\"App Store\",\"userid\":0,\"packageName\":\"com.yoyo.yushen\",\"imei\":\"\",\"versioncode\":36,\"mobile_model\":\"x86_64\",\"os_type\":2}";
		String encode = aesEncryptString(code);
		System.err.println(encode);
		
		//解密测试------>将对应的加密头数据解密成对应的明文		
		//encode = "HxOWP7h2CQjO5id1jSem9Z+o3LTM+AHgwRUAyndjIxsyH9XhIrNeAl0FPw5J0U3A1ULY4LdJjGNIqgUUl3BS9mHowCMMm3pKNNxOhin7xgkwS3e3xD91dCXZdyYRvN4TMsci+5l57LPb3KBAsoMxtbxZdjPu6RHxozL6IfzOuQZUepHme86oZ7obh1LeYYDWV1BcwMkWITdNNFDIqrm4/AWDu+5xoWS4geLH4urd5qS4eos8TU2EtkXBUxpuawT30WuoBCxKaZX6Qy9U3mnt46Mmqa2NTcn7IQU8PtNCjOM=";
		code = aesDecryptString(encode);
		System.err.println(code);
		
	}

	
}
