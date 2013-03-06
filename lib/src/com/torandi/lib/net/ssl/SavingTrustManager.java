package com.torandi.lib.net.ssl;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


public class SavingTrustManager implements X509TrustManager {

	private final X509TrustManager tm;
	private X509Certificate[] chain;
	private X509Certificate trusted_cert = null;

	public SavingTrustManager(X509TrustManager tm) {
		this.tm = tm;
	}
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		tm.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		this.chain = chain;
		if(trusted_cert != null) {
			System.out.println("Start check + "+chain.length);
			chain[0].checkValidity();
			System.out.println("Cert valid");
			try {
				chain[0].verify(trusted_cert.getPublicKey());
				return;
			} catch (Exception e) {
				throw new CertificateException("Failed to validate key against trusted_cert");
			}
		} else {
			System.out.println("trusted cert is null");
		}
		tm.checkServerTrusted(chain, authType);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		if(trusted_cert != null ) {
			return new X509Certificate[] { trusted_cert };
		} else {
			return new X509Certificate[] { };
		}
	}
	
	public void addTrustedCert(X509Certificate cert) {
		trusted_cert = cert;
	}
	
	public X509Certificate[] chain() {
		return chain;
	}

}
