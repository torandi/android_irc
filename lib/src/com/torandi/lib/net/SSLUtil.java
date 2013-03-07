package com.torandi.lib.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;


public class SSLUtil {

	private SSLContext sslContext;
	private SSLSocketFactory sslSocketFactor = null;
	private SSLServerSocketFactory sslServerSocketFactor = null;
	private SavingTrustManager sm;
	private KeyStore keystore;

	public SSLUtil(String keystore_file, String password) throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException, FileNotFoundException, IOException {
		this(createKeyStore(new FileInputStream(new File(keystore_file)), password), password);
	}
	
	public SSLUtil(KeyStore keystore, String password) throws UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, KeyManagementException {
		X509KeyManager km = createKeyManager(keystore, password);
		X509TrustManager tm = createTrustManager(keystore);
		sm = new SavingTrustManager(tm);

		this.keystore = keystore;
		sslContext = createSSLContext(km, sm);
	}
	
	public SSLUtil(InputStream keystore, String password) throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException, IOException {
		this(createKeyStore(keystore, password), password);
	}

	public SavingTrustManager getTrustManager() {
		return sm;
	}
	
	public SSLSocket connect(String host, int port) throws UnknownHostException, IOException {
		if(sslSocketFactor == null) {
			sslSocketFactor = (SSLSocketFactory) sslContext.getSocketFactory();
		}
		return (SSLSocket) sslSocketFactor.createSocket(host, port);
	}

	/**
	 * Starts to listen for ssl connections on the given port
	 * EnabledProtocols are set to TLS only
	 * 
	 * @param port
	 * @return A new SSLServerSocket
	 * @throws IOException
	 */
	public SSLServerSocket listen(int port) throws IOException {
		if(sslServerSocketFactor == null) {
			sslServerSocketFactor = (SSLServerSocketFactory) sslContext.getServerSocketFactory();
		}
		SSLServerSocket ss = (SSLServerSocket) sslServerSocketFactor.createServerSocket(port);
		ss.setEnabledProtocols(new String[] { "TLSv1" });
		return ss;
	}

	/**
	 * Return the fingerprint for a given cert
	 * @param cert
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateEncodingException
	 */
	public static String getFingerPrint(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] der = cert.getEncoded();
		md.update(der);
		byte[] digest = md.digest();
		return hexify(digest);
	}

	/**
	 * Add a certificate to the list of trusted certs
	 * 
	 * @param cert 
	 * @throws KeyStoreException
	 */
	public void addTrustedCert(X509Certificate cert) throws KeyStoreException {
		String dn = cert.getSubjectDN().getName();
		keystore.setEntry(dn, new KeyStore.TrustedCertificateEntry(cert), null);
		
		sm.setTemporaryTrustedCert(cert);
	}
	
	/**
	 *  Save the current keystore to disk
	 * @param file
	 * @param password
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public void saveKeyStore(String file, String password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		OutputStream os = new FileOutputStream(new File(file));
		keystore.store(os, password.toCharArray());
		os.close();
	}
	
	public void saveKeyStore(OutputStream os, String password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		keystore.store(os, password.toCharArray());
	}

	/**
	 * Create a keystore from a input stream
	 * @param is InputStream to use
	 * @param password Password for the input stream
	 * @return The created keystore
	 * @throws KeyStoreException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static KeyStore createKeyStore(InputStream is, String password) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore keystore = KeyStore.getInstance("JKS", "SUN");
		keystore.load(is, password.toCharArray());
		return keystore;
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
	
	private static SSLContext createSSLContext(KeyManager km, TrustManager tm ) throws KeyManagementException, NoSuchAlgorithmException {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(new KeyManager[] { km }, new TrustManager[] { tm }, null);
		
		return sslContext;
	}
	
	private static X509KeyManager createKeyManager(KeyStore keystore, String password) throws NoSuchAlgorithmException, NoSuchProviderException, UnrecoverableKeyException, KeyStoreException {
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keystore, password.toCharArray());
		for( KeyManager km : keyManagerFactory.getKeyManagers() ) {
			if( km instanceof X509KeyManager ) {
				return (X509KeyManager) km;
			}
		}
		
		throw new NullPointerException();
	}
	
	private static X509TrustManager createTrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
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
