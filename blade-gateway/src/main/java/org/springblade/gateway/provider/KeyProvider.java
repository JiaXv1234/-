/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 * <p>
 * Use of this software is governed by the Commercial License Agreement
 * obtained after purchasing a license from BladeX.
 * <p>
 * 1. This software is for development use only under a valid license
 * from BladeX.
 * <p>
 * 2. Redistribution of this software's source code to any third party
 * without a commercial license is strictly prohibited.
 * <p>
 * 3. Licensees may copyright their own code but cannot use segments
 * from this software for such purposes. Copyright of this software
 * remains with BladeX.
 * <p>
 * Using this software signifies agreement to this License, and the software
 * must not be used for illegal purposes.
 * <p>
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY. The author is
 * not liable for any claims arising from secondary or illegal development.
 * <p>
 * Author: Chill Zhuang (bladejava@qq.com)
 */
package org.springblade.gateway.provider;

import org.springblade.core.launch.props.BladeProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 超级密钥加解密工具类
 *
 * @author BladeX
 */
public class KeyProvider {
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/**
	 * API Key 前缀
	 */
	private static final String API_KEY_PREFIX = "ak-";
	/**
	 * API Key 配置项
	 */
	private static final String BLADE_KEY_ENABLED = "blade.key.enabled";
	/**
	 * API Key 配置项
	 */
	private static final String BLADE_KEY_CRYPTO_KEY = "blade.key.crypto-key";
	/**
	 * 随机字符串因子
	 */
	private static final String RANDOM_FACTOR = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	/**
	 * 安全随机数生成器
	 */
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	/**
	 * PKCS7 块大小
	 */
	private static final int BLOCK_SIZE = 16;
	/**
	 * Hex 编码字符
	 */
	private static final byte[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * 判断是否为合法 API Key
	 *
	 * @param auth 认证字符串
	 * @return 是否为 API Key
	 */
	public static boolean isApiKey(@Nullable String auth, BladeProperties bladeProperties) {
		// 检查功能是否启用
		String enabled = bladeProperties.getEnvironment().getProperty(BLADE_KEY_ENABLED);
		if (!Boolean.parseBoolean(enabled)) {
			return false;
		}
		// 检查前缀并解析
		if (auth != null && auth.startsWith(API_KEY_PREFIX)) {
			String cryptoKey = bladeProperties.getEnvironment().getProperty(BLADE_KEY_CRYPTO_KEY);
			String parsed = parseKey(auth, cryptoKey);
			return parsed != null;
		}
		return false;
	}

	/**
	 * 生成 API Key
	 *
	 * @param cryptoKey 加密密钥
	 * @return 带前缀的加密 Key
	 */
	public static String generateKey(String cryptoKey) {
		return API_KEY_PREFIX + encryptToHex(randomUUID(), cryptoKey);
	}

	/**
	 * 解析 API Key
	 *
	 * @param key       带前缀的加密 Key
	 * @param cryptoKey 加密密钥
	 * @return 解密后的原始值
	 */
	@Nullable
	public static String parseKey(@Nullable String key, String cryptoKey) {
		if (isBlank(key) || !key.startsWith(API_KEY_PREFIX)) {
			return null;
		}
		try {
			String encryptedPart = key.substring(API_KEY_PREFIX.length());
			return decryptFormHexToString(encryptedPart, cryptoKey);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 加密并转换为十六进制字符串
	 *
	 * @param content    明文内容
	 * @param aesTextKey AES 密钥字符串
	 * @return 十六进制加密字符串
	 */
	public static String encryptToHex(String content, String aesTextKey) {
		return hexEncode(encrypt(content.getBytes(DEFAULT_CHARSET), aesTextKey));
	}

	/**
	 * 从十六进制字符串解密并返回明文字符串
	 *
	 * @param content    十六进制加密字符串
	 * @param aesTextKey AES 密钥字符串
	 * @return 解密后的明文字符串
	 */
	@Nullable
	public static String decryptFormHexToString(@Nullable String content, String aesTextKey) {
		byte[] hexBytes = decryptFormHex(content, aesTextKey);
		if (hexBytes == null) {
			return null;
		}
		return new String(hexBytes, DEFAULT_CHARSET);
	}

	/**
	 * 从十六进制字符串解密并返回字节数组
	 *
	 * @param content    十六进制加密字符串
	 * @param aesTextKey AES 密钥字符串
	 * @return 解密后的字节数组
	 */
	@Nullable
	public static byte[] decryptFormHex(@Nullable String content, String aesTextKey) {
		if (isBlank(content)) {
			return null;
		}
		return decrypt(hexDecode(content.getBytes(DEFAULT_CHARSET)), aesTextKey);
	}

	/**
	 * 加密字节数组
	 *
	 * @param content    明文字节数组
	 * @param aesTextKey AES 密钥字符串
	 * @return 加密后的字节数组
	 */
	public static byte[] encrypt(byte[] content, String aesTextKey) {
		return aes(pkcs7Encode(content), Objects.requireNonNull(aesTextKey).getBytes(DEFAULT_CHARSET), Cipher.ENCRYPT_MODE);
	}

	/**
	 * 解密字节数组
	 *
	 * @param content    加密的字节数组
	 * @param aesTextKey AES 密钥字符串
	 * @return 解密后的字节数组
	 */
	public static byte[] decrypt(byte[] content, String aesTextKey) {
		return pkcs7Decode(aes(content, Objects.requireNonNull(aesTextKey).getBytes(DEFAULT_CHARSET), Cipher.DECRYPT_MODE));
	}

	/**
	 * AES 加解密核心方法
	 */
	private static byte[] aes(byte[] data, byte[] aesKey, int mode) {
		Assert.isTrue(aesKey.length == 32, "IllegalAesKey, aesKey's length must be 32");
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
			IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
			cipher.init(mode, keySpec, iv);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ==================== 内联工具方法 ====================

	/**
	 * 生成指定长度的随机字符串
	 */
	public static String randomKey(int count) {
		char[] buffer = new char[count];
		for (int i = 0; i < count; i++) {
			buffer[i] = RANDOM_FACTOR.charAt(SECURE_RANDOM.nextInt(RANDOM_FACTOR.length()));
		}
		return new String(buffer);
	}

	/**
	 * 生成无横线的 UUID
	 */
	private static String randomUUID() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return new UUID(random.nextLong(), random.nextLong()).toString().replace("-", "");
	}

	/**
	 * 检查字符串是否为空或空白
	 */
	private static boolean isBlank(@Nullable String str) {
		if (str == null || str.isEmpty()) {
			return true;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 字节数组转十六进制字符串
	 */
	private static String hexEncode(byte[] data) {
		int len = data.length;
		byte[] out = new byte[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			out[j++] = HEX_DIGITS[(0xF0 & data[i]) >>> 4];
			out[j++] = HEX_DIGITS[0x0F & data[i]];
		}
		return new String(out, DEFAULT_CHARSET);
	}

	/**
	 * 十六进制字节数组解码为原始字节数组
	 */
	private static byte[] hexDecode(byte[] data) {
		int len = data.length;
		if ((len & 0x01) != 0) {
			throw new IllegalArgumentException("hexBinary needs to be even-length: " + len);
		}
		byte[] out = new byte[len >> 1];
		for (int i = 0, j = 0; j < len; i++) {
			int f = Character.digit(data[j++], 16) << 4;
			f |= Character.digit(data[j++], 16);
			out[i] = (byte) (f & 0xFF);
		}
		return out;
	}

	/**
	 * PKCS7 编码（填充）
	 */
	private static byte[] pkcs7Encode(byte[] src) {
		int count = src.length;
		int amountToPad = BLOCK_SIZE - (count % BLOCK_SIZE);
		byte pad = (byte) (amountToPad & 0xFF);
		byte[] dest = new byte[count + amountToPad];
		System.arraycopy(src, 0, dest, 0, count);
		Arrays.fill(dest, count, dest.length, pad);
		return dest;
	}

	/**
	 * PKCS7 解码（去填充）
	 */
	private static byte[] pkcs7Decode(byte[] decrypted) {
		int pad = decrypted[decrypted.length - 1];
		if (pad < 1 || pad > BLOCK_SIZE) {
			pad = 0;
		}
		if (pad > 0) {
			return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
		}
		return decrypted;
	}
}
