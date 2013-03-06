package com.torandi.lib.net.ssl;

import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;

public class SSLUtil {
	
	public static String getFingerPrint(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] der = cert.getEncoded();
		md.update(der);
		byte[] digest = md.digest();
		return hexify(digest);
	}

	private static String hexify (byte bytes[]) {

		char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', 
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

		StringBuffer buf = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; ++i) {
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
			buf.append(":");
		}
		buf.deleteCharAt(buf.length() - 1);

		return buf.toString();
	}
	
	public static SSLContext createSSLContext(KeyManager km, TrustManager tm ) throws KeyManagementException, NoSuchAlgorithmException {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(new KeyManager[] { km }, new TrustManager[] { tm }, null);
		
		return sslContext;
	}
	
	public static X509KeyManager createKeyManager(KeyStore keystore, String password) throws NoSuchAlgorithmException, NoSuchProviderException, UnrecoverableKeyException, KeyStoreException {
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keystore, password.toCharArray());
		for( KeyManager km : keyManagerFactory.getKeyManagers() ) {
			if( km instanceof X509KeyManager ) {
				return (X509KeyManager) km;
			}
		}
		
		throw new NullPointerException();
	}
	
	public static X509TrustManager createTrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keystore);
		for( TrustManager tm : trustManagerFactory.getTrustManagers() ) {
			if( tm instanceof X509TrustManager ) {
				return (X509TrustManager) tm;
			}
		}
		
		throw new NullPointerException();
	}
}
