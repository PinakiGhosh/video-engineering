package com.ott.drm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.ott.db.InMemoryDB;
import com.ott.utils.Keys;

public class CoverWithDRM implements Runnable{
	
public static final int AES_Key_Size = 256;
	
	Cipher rsaCipher; 
	Cipher aesCipher;
	byte[] aesKey;
	SecretKeySpec aeskeySpec;
	
	/**
	 * Constructor: creates ciphers
	 */
	public CoverWithDRM() {
		try {
			// create RSA public key cipher
			rsaCipher = Cipher.getInstance("RSA");
			 // create AES shared key cipher
		    aesCipher = Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
	   
	}
	
	/**
	 * Creates a new AES key
	 */
	public void makeKey() throws NoSuchAlgorithmException {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
	    kgen.init(AES_Key_Size);
	    SecretKey key = kgen.generateKey();
	    aesKey = key.getEncoded();
	    aeskeySpec = new SecretKeySpec(aesKey, "AES");
	}

	/**
	 * Decrypts an AES key from a file using an RSA private key
	 */
	public void loadKey(File in, File privateKeyFile) throws GeneralSecurityException, IOException {
		// read private key to be used to decrypt the AES key
		byte[] encodedKey = new byte[(int)privateKeyFile.length()];
		new FileInputStream(privateKeyFile).read(encodedKey);
		
		// create private key
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = kf.generatePrivate(privateKeySpec);
		
		// read AES key
		rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
		aesKey = new byte[AES_Key_Size/8];
		CipherInputStream is = new CipherInputStream(new FileInputStream(in), rsaCipher);
		is.read(aesKey);
		aeskeySpec = new SecretKeySpec(aesKey, "AES");
	}
	
	/**
	 * Encrypts the AES key to a file using an RSA public key
	 */
	public void saveKey(File out, File publicKeyFile) throws IOException, GeneralSecurityException {
		// read public key to be used to encrypt the AES key
		byte[] encodedKey = new byte[(int)publicKeyFile.length()];
		new FileInputStream(publicKeyFile).read(encodedKey);
		
		// create public key
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey publicKey = kf.generatePublic(publicKeySpec);
		
		// write AES key
		rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
		CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out), rsaCipher);
		os.write(aesKey);
		os.close();
	}
	
	/**
	 * Encrypts and then copies the contents of a given file.
	 */
	private void encrypt(File in, File out) throws IOException, InvalidKeyException {
		aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
		FileInputStream is = new FileInputStream(in);
		CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out), aesCipher);
		copy(is, os);
		os.close();
	}
	
	
	/**
	 * Copies a stream.
	 */
	private void copy(InputStream is, OutputStream os) throws IOException {
		int i;
		byte[] b = new byte[1024];
		while((i=is.read(b))!=-1) {
			os.write(b, 0, i);
		}
	}

	@Override
	public void run() {
		String clearFolderPath = InMemoryDB.getInstance().getKey(Keys.process_dir_clear);
		String encryptedFolderPath = InMemoryDB.getInstance().getKey(Keys.process_dir_encrypted);
		System.out.println(clearFolderPath);
		File clearFolder = new File(clearFolderPath);
		if (clearFolder.exists()) {
			for (File inFile : clearFolder.listFiles()) {
				File outFile = new File(encryptedFolderPath+File.separator+inFile.getName());
				System.out.println("Un-encrypted file: "+ inFile.getAbsolutePath());
				System.out.println("Encrypted file: "+ outFile.getAbsolutePath());
				try {
					if (!(inFile.getName().endsWith(Keys.HLS) || inFile.getName().endsWith(Keys.DASH) || inFile.getName().endsWith(Keys.SS)))
						encrypt(inFile, outFile);
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
