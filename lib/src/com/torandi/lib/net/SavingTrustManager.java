package com.torandi.lib.net;
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
			chain[0].checkValidity();
			try {
				chain[0].verify(trusted_cert.getPublicKey());
				return;
			} catch (Exception e) { } /* fall through to normal check */
		}
		
		tm.checkServerTrusted(chain, authType);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return tm.getAcceptedIssuers();
	}
	
	public void setTemporaryTrustedCert(X509Certificate cert) {
		trusted_cert = cert;
	}
	
	public X509Certificate[] chain() {
		return chain;
	}

}
