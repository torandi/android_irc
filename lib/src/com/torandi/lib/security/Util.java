package com.torandi.lib.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Util {
	private static String hexify (byte bytes[], boolean fingerprint) {

		char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', 
				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

		StringBuffer buf = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; ++i) {
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
			if(fingerprint) buf.append(":");
		}
		if(fingerprint) buf.deleteCharAt(buf.length() - 1);

		return buf.toString();
	}

	public static String getFingerprint(byte[] bytes) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(bytes);
		byte[] digest = md.digest();
		return hexify(digest, true);
	}
	
	public static String randomString(int length) throws NoSuchAlgorithmException {
		SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
		byte[] b = new byte[length];
		rnd.nextBytes(b);
		return hexify(b, false);
	}
	
	public static String toHex(byte[] bytes) {
		return hexify(bytes, false);
	}
	
	public static byte[] fromHex(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
}
