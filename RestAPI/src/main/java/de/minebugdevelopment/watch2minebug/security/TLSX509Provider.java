package de.minebugdevelopment.watch2minebug.security;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.net.ssl.*;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * This TLS provider can be used to encrypt the socket communication between client and server. Depending on how
 * initialized, it can either serve client or server side key and trust management. Use
 * {@link TLSX509Provider#TLSX509Provider(String, String, String, String)} or
 * {@link TLSX509Provider#TLSX509Provider(File, String, String, File)} to
 * initialize this provider for client side and {@link TLSX509Provider#TLSX509Provider(String, String, String)} or
 * {@link TLSX509Provider#TLSX509Provider(File, String, String)} for server side operation.
 * Server side means, that every incoming connection is trusted and a uniquely generated self-signed server certificate
 * is used to prove authenticity to clients if none is already present.
 * Client side means, that every certificate signed by a public CA is automatically trusted and self-signed certificates
 * are rejected if not present in the custom trust store. Those certificates can be trusted by calling
 * {@link TLSX509Provider#trustCertificate(X509Certificate)} or
 * {@link TLSX509Provider#trustCertificate(X509Certificate[])}. Furthermore, clients create their own unique self-signed
 * certificate.
 * The key and trust store files are automatically created and stored in the location specified in each constructor. A
 * custom passphrase is necessary to prevent unauthorized to those stores. The file format PKCS12 is used.
 * If you want to use this class with lets encrypt on the server side, you have to generate a PKCS12 file containing
 * the server certificate. Create a new instance of this class if the PKCS12 file has been updated during runtime.
 * Note: Please set and remember the passphrase and certificate alias as they are needed in the constructor.
 * Note: The same passphrase is used for key and trust store.
 * Note: Uses TLSv1.3 for Java 11 and above.
 * Note: Tested Java versions: 9.0.4, 13.0.2 and 17.0.2
 * Attention: This class requires the latest version of the bouncycastle lightweight java api.
 * Note: Tested bouncycastle versions: jdk15on-1.70
 * @author ChrTopf
 */
public class TLSX509Provider {

	//TODO: what if the hostname of the machine changes?

	protected String KEY_STORE_TYPE = "PKCS12";
	public void setKeyStoreType(String type){
		KEY_STORE_TYPE = type;
	}

	protected String SIGNATURE_ALGORITHM = "SHA256withRSA"; //TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384
	public void setSignatureAlgorithm(String algorithm){
		SIGNATURE_ALGORITHM = algorithm;
	}

	protected String KEY_ALGORITHM = "RSA"; //Note: DSA ist deprecated in TLS1.3
	public void setKeyAlgorithm(String algorithm){
		KEY_ALGORITHM = algorithm;
	}

	protected int KEY_LENGTH = 4096;
	public void setKeyLength(int length){
		KEY_LENGTH = length;
	}

	protected String TLS_VERSION = (getVersion() > 10) ? "TLSv1.3" : "TLSv1.2";
	public void setTLSVersion(String version){
		TLS_VERSION = version;
	}

	private static String[] SUPPORTED_CIPHER_SUITES;

	private final char[] passphrase;
	private File keyStoreFile;
	private File trustStoreFile;
	private String certificateAlias;
	private final boolean serverSide;

	private KeyManagerFactory keyManager;
	private MHSTrustManager trustManager;

	/**
	 * Creates a new client side instance of this class. The passphrase is used for both stores.
	 * @param keyStorePath The path where the client key store should be located. This file gets generated
	 *                     automatically.
	 * @param passphrase The passphrase that should be used to access the trust store file.
	 * @param certificateAlias A custom alias e.g. name for the client side certificate.
	 * @param trustStorePath The path where the client trust store should be located. This file is automatically
	 *                       generated if not present yet.
	 */
	public TLSX509Provider(String keyStorePath, String passphrase, String certificateAlias, String trustStorePath){
		//add the lightweight bouncy castle api as a security provider
		Security.addProvider(new BouncyCastleProvider());
		//initialize the basic stuff
		keyStoreFile = new File(keyStorePath);
		trustStoreFile = new File(trustStorePath);
		this.passphrase = passphrase.toCharArray();
		this.certificateAlias = certificateAlias;
		serverSide = false;
	}

	/**
	 * Creates a new client side instance of this class. The passphrase is used for both stores.
	 * @param keyStoreFile The key store file. Gets generated automatically if not present.
	 * @param passphrase The passphrase that should be used to access the trust store file.
	 * @param certificateAlias A custom alias e.g. name for the client side certificate.
	 * @param trustStoreFile The client trust store file. This file is automatically generated if not present yet.
	 */
	public TLSX509Provider(File keyStoreFile, String passphrase, String certificateAlias, File trustStoreFile){
		//add the lightweight bouncy castle api as a security provider
		Security.addProvider(new BouncyCastleProvider());
		//initialize the basic stuff
		this.keyStoreFile = keyStoreFile;
		this.trustStoreFile = trustStoreFile;
		this.passphrase = passphrase.toCharArray();
		this.certificateAlias = certificateAlias;
		serverSide = false;
	}

	/**
	 * Creates a new server side instance of this class.
	 * @param keyStorePath The path where the server key store should be located. This file is automatically generated
	 *                     with a self-signed certificate if not present yet.
	 * @param passphrase The passphrase that should be used to access the key store file.
	 * @param certificateAlias The alias e.g. name of the certificate stored in the key store file.
	 */
	public TLSX509Provider(String keyStorePath, String passphrase, String certificateAlias) {
		//add the lightweight bouncy castle api as a security provider
		Security.addProvider(new BouncyCastleProvider());
		//initialize the basic stuff
		keyStoreFile = new File(keyStorePath);
		this.passphrase = passphrase.toCharArray();
		this.certificateAlias = certificateAlias;
		serverSide = true;
	}

	/**
	 * Creates a new server side instance of this class.
	 * @param keyStoreFile The server key store file. This file is automatically generated with a self-signed
	 *                     certificate if not present yet.
	 * @param passphrase The passphrase that should be used to access the key store file.
	 * @param certificateAlias The alias e.g. name of the certificate stored in the key store file.
	 */
	public TLSX509Provider(File keyStoreFile, String passphrase, String certificateAlias) {
		//add the lightweight bouncy castle api as a security provider
		Security.addProvider(new BouncyCastleProvider());
		//initialize the basic stuff
		this.keyStoreFile = keyStoreFile;
		this.passphrase = passphrase.toCharArray();
		this.certificateAlias = certificateAlias;
		serverSide = true;
	}

	public SSLSocketFactory getClientFactory() {
		//check if the trust manager has already been initialized
		if(keyManager != null && trustManager != null){
			try{
				//initialize the ssl context for the client
				SSLContext context = SSLContext.getInstance(TLS_VERSION);
				context.init(keyManager.getKeyManagers(), new TrustManager[]{trustManager}, null); //use the default key manager for client side
				context.createSSLEngine();
				return context.getSocketFactory();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}

	public SSLServerSocketFactory getServerFactory() {
		//check if the key and trust managers has already been initialized
		if(keyManager != null && trustManager != null){
			try{
				//initialize the ssl context for the server
				SSLContext context = SSLContext.getInstance(TLS_VERSION);
				context.init(keyManager.getKeyManagers(), new TrustManager[]{trustManager}, null);
				context.createSSLEngine();
				return context.getServerSocketFactory();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}

	public SSLContext getContext() {
		//check if the key and trust managers has already been initialized
		if(keyManager != null && trustManager != null){
			try{
				//initialize the ssl context for the server
				SSLContext context = SSLContext.getInstance(TLS_VERSION);
				context.init(keyManager.getKeyManagers(), new TrustManager[]{trustManager}, null);
				context.createSSLEngine();
				return context;
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}

	public boolean isServerTrusted(X509Certificate[] certificate, String authType) {
		try{
			trustManager.checkServerTrusted(certificate, authType);
			return true;
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Execute this long-running method for initializing the TLS provider. Note: A lot can go wrong here, so do not give
	 * up fast. :)
	 * @return returns true on success and false if an error occurred.
	 */
	public boolean initialize(){
		//prepare KeyStore instance
		KeyStore keyStore = null;
		//check if a new store needs to be generated first
		if(!keyStoreFile.exists()){
			try {
				//create keypair with the default algorithm
				KeyPairGenerator keypairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM /*, "BC"*/);
				keypairGen.initialize(KEY_LENGTH, new SecureRandom());
				KeyPair keypair = keypairGen.generateKeyPair();
				//get the hostname from background thread
				String hostname = getHostname();
				//create self-signed certificate
				X509Certificate cert = generateX509Cert(hostname, keypair);
				//create a new keystore
				keyStore = generateKeyStore(cert, certificateAlias, keypair, passphrase);
				//save certificate and keys in a keystore file
				saveStore(keyStore, keyStoreFile, passphrase);
				System.out.println("File generated and saved.");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}else{
			try{
				keyStore = loadStore(keyStoreFile, passphrase);
				if(keyStore.getCertificate(certificateAlias) != null)
					System.out.println("Key store loaded.");
				else
					throw new KeyStoreException("The key store does not contain a certificate with the specified alias.");
			}catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}
		//initialize the key manager factory now
		try {
			// KeyManagers decide which key material to use
			keyManager = KeyManagerFactory.getInstance("PKIX");
			keyManager.init(keyStore, passphrase);
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}

		//initialize the trust manager
		trustManager = new MHSTrustManager(this);
		if(!serverSide){
			return trustManager.initialize(trustStoreFile, passphrase);
		}
		return true;
	}

	/**
	 * This method takes the last certificate of the given chain (array) and adds it to the trust store.
	 * @param chain The chain of certificates.
	 * @return returns true on success and false if the certificate could not be added.
	 */
	public boolean trustCertificate(X509Certificate[] chain){
		if(trustManager == null || chain == null || chain.length == 0){
			return false;
		}
		//trust the last certificate
		X509Certificate lastCrt = chain[chain.length - 1];
		return trustManager.trustCertificate(lastCrt);
	}

	/**
	 * This method adds a certificate to the trust store.
	 * @param crt The certificate to be trusted.
	 * @return returns true on success and false if the certificate could not be added.
	 */
	public boolean trustCertificate(X509Certificate crt){
		if(trustManager == null || crt == null){
			return false;
		}
		//trust the certificate
		return trustManager.trustCertificate(crt);
	}


	/**
	 * Get a list of cipher suites supported by this TLS provider (which are currently considered as secure).
	 * @return returns a list of supported suites or null if an error occurred.
	 */
	public static String[] getSupportedCipherSuites(){
		//TODO: return only secure cipher suites
		if(SUPPORTED_CIPHER_SUITES != null){
			return SUPPORTED_CIPHER_SUITES;
		}
		try {
			SUPPORTED_CIPHER_SUITES = SSLContext.getDefault().getSocketFactory().getSupportedCipherSuites();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return SUPPORTED_CIPHER_SUITES;
	}

	private X509Certificate generateX509Cert(String hostname, KeyPair keyPair) throws IOException, OperatorCreationException, CertificateException {
		SecureRandom random = new SecureRandom();

		// fill in certificate fields
		X500Name subject = new X500NameBuilder(BCStyle.INSTANCE)
				.addRDN(BCStyle.CN, hostname)
				.build();
		byte[] id = new byte[20];
		random.nextBytes(id);
		BigInteger serial = new BigInteger(160, random);
		X509v3CertificateBuilder certificate = new JcaX509v3CertificateBuilder(
				subject,
				serial,
				new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000),
				new Date(System.currentTimeMillis() + 2L * 365 * 24 * 60 * 60 * 1000),
				subject,
				keyPair.getPublic());
		certificate.addExtension(Extension.subjectKeyIdentifier, false, id);
		certificate.addExtension(Extension.authorityKeyIdentifier, false, id);
		BasicConstraints constraints = new BasicConstraints(true);
		certificate.addExtension(Extension.basicConstraints, true, constraints.getEncoded());
		KeyUsage usage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature);
		certificate.addExtension(Extension.keyUsage, false, usage.getEncoded());
		ExtendedKeyUsage usageEx = new ExtendedKeyUsage(new KeyPurposeId[] {
				KeyPurposeId.id_kp_serverAuth,
				KeyPurposeId.id_kp_clientAuth
		});
		certificate.addExtension(Extension.extendedKeyUsage, false, usageEx.getEncoded());

		// build BouncyCastle certificate
		ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
				.build(keyPair.getPrivate());
		X509CertificateHolder holder = certificate.build(signer);

		// convert to JRE certificate
		JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
		converter.setProvider(new BouncyCastleProvider());
		return converter.getCertificate(holder);
	}

	private KeyStore generateKeyStore(X509Certificate crt, String certificateAlias, KeyPair keyPair, char[] passphrase) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
		java.security.cert.Certificate[] outChain = { crt };
		KeyStore outStore = KeyStore.getInstance(KEY_STORE_TYPE);
		outStore.load(null, passphrase);
		outStore.setKeyEntry(certificateAlias, keyPair.getPrivate(), passphrase, outChain);
		return outStore;
	}

	public void saveStore(KeyStore outStore, File keyStore, char[] passphrase) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
		OutputStream outputStream = new FileOutputStream(keyStore);
		outStore.store(outputStream, passphrase);
		outputStream.flush();
		outputStream.close();
	}

	public KeyStore loadStore(File keyStore, char[] passphrase) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
		//TODO: close stream!!!
		KeyStore inStore = KeyStore.getInstance(KEY_STORE_TYPE);
		FileInputStream input = new FileInputStream(keyStore);
		inStore.load(input, passphrase);
		input.close();
		return inStore;
	}

	private static int getVersion() {
		String version = System.getProperty("java.version");
		if(version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			int dot = version.indexOf(".");
			if(dot != -1) {
				version = version.substring(0, dot);
			}
		}
		return Integer.parseInt(version);
	}

	private String getHostname(){
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
