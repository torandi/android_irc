package com.torandi.lib.security;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * Handles RSA crypto
 * 
 * Process of using:
 * 1. rsa=new RSA();
 * 2. rsa.setKeys(pubMod,pubExp,privMod,privExp) or rsa.generateKeys();
 * 3. rsa.init(); 
 * 4. ready to encrypt/decrypt
 * 
 * 
 * @author Andreas Tarandi
 */
public class RSA {
    private KeyPair myPair=null;
    private Cipher decrypt,encrypt;
    private KeyFactory fact = null;
    
    private static final String ALGORITH="RSA";
    private static final int KEYSIZE=2048;
    
    private boolean encrypt_ready=false;
    
    public RSA() {
        try {
	        encrypt = Cipher.getInstance(ALGORITH);
	        decrypt = Cipher.getInstance(ALGORITH);
			fact = KeyFactory.getInstance(ALGORITH);
        } catch (Exception e) {
            System.err.println("An error occured while initializing the RSA object:");
            e.printStackTrace();
        }
    }
    
    public void init() {
        try {
            decrypt.init(Cipher.DECRYPT_MODE,myPair.getPrivate());
            //selfTest();
	    } catch (Exception e) {
	        System.err.println("An error occured while initializing the RSA object:");
	        e.printStackTrace();
	    }
    }
    
	public void selfTest() {
        try {
        initEncryption(getPublicKey());
        } catch(Exception e) {
            System.err.println("Failed to init encryption");
        }
         String random="";
         while(random.getBytes().length < 117) {
             random+=randomChar();
         }
         byte[] encrypted=null;
         try {
            encrypted = encrypt(random);
         } catch (Exception e) {
             System.err.println("Self test failed: Failed to encrypt");
             e.printStackTrace();
         } 
         if(encrypted!=null) {
             try {
                 String decrypted = decrypt(encrypted);
                 if(!random.equals(decrypted)) {
                     System.err.println("Self test failed: Strings not equal: (in:"+random+", out: "+decrypted+")");
                 }
             } catch (Exception e) {
                 System.err.println("Self test failed: Failed to decrypt");
                 e.printStackTrace();
             } 
         }
         encrypt_ready=false;
    }
   
	/* Only used for self test */
    private char randomChar() {
        return (char)(Math.floor(Math.random()*94)+32); //between ascii 32 och 125
    }

    public boolean canEncrypt() {
        return encrypt_ready;
    }
    
    public static String getFingerprint(PublicKey pk) throws NoSuchAlgorithmException {
    	return Util.getFingerprint(pk.getEncoded());
    }
    
    public String getFingerprint() throws NoSuchAlgorithmException {
    	return RSA.getFingerprint(myPair.getPublic());
    }

    public String decrypt(byte[] in_bytes) throws IllegalBlockSizeException, BadPaddingException {
       byte[] bytes = decrypt.doFinal(in_bytes);
       return new String(bytes);
    }
    
    public byte[] decryptToBytes(byte[] in_bytes) throws IllegalBlockSizeException, BadPaddingException {
       return decrypt.doFinal(in_bytes);
    }
    
    public void setKeys(BigInteger pubMod,BigInteger pubExp,BigInteger privMod,BigInteger privExp) throws InvalidKeySpecException {
		RSAPublicKeySpec pub = new RSAPublicKeySpec(pubMod, pubExp);
		RSAPrivateKeySpec priv = new RSAPrivateKeySpec(privMod, privExp);
		myPair=new KeyPair(fact.generatePublic(pub),fact.generatePrivate(priv));
    }
    
    public PublicKey createPublicKey(BigInteger pubMod, BigInteger pubExp) throws InvalidKeySpecException {
		RSAPublicKeySpec pub = new RSAPublicKeySpec(pubMod, pubExp);
		return fact.generatePublic(pub);
    }
    
    /**
     * Generate keys
     */
    public void generateKeys() {
            try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITH);
            kpg.initialize(KEYSIZE);
            myPair = kpg.generateKeyPair();
        
            } catch (Exception e) {
                System.err.println("Failed to generate keys");
                e.printStackTrace();
            }
    }
    
    public RSAPublicKey getPublicKey() {
        return (RSAPublicKey)myPair.getPublic();
    }
    
    public RSAPrivateKey getPrivateKey() {
        return (RSAPrivateKey)myPair.getPrivate();
    }
    
    
    public RSAPublicKeySpec getPublicKeySpec() {
    	KeyFactory fact;
		try {
			fact = KeyFactory.getInstance(ALGORITH);
	        return fact.getKeySpec(myPair.getPublic(),RSAPublicKeySpec.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public RSAPrivateKeySpec getPrivateKeySpec() {
    	KeyFactory fact;
		try {
			fact = KeyFactory.getInstance(ALGORITH);
	        return fact.getKeySpec(myPair.getPrivate(),RSAPrivateKeySpec.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
   /**
    * Initializes encryption with public key
    * @param pk
    * @throws InvalidKeyException
    */
   public void initEncryption(PublicKey pk) throws InvalidKeyException {
       encrypt.init(Cipher.ENCRYPT_MODE,pk);
       encrypt_ready=true;
   }  
   
   public byte[] encrypt(String msg) throws IllegalBlockSizeException, BadPaddingException {
            if(encrypt_ready) {
                return encrypt.doFinal(msg.getBytes());    
            } else {
                return null;
            }
   }
   
    public byte[] encrypt(byte[] bytes) throws IllegalBlockSizeException, BadPaddingException {
            if(encrypt_ready) {
                return encrypt.doFinal(bytes);    
            } else {
                return null;
            }
   }
    
    
}