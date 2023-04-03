package de.minebugdevelopment.watch2minebug.security;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.lang.reflect.Array;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This class provides a trust manager for TLS certificate authorization. By using this trust manager on a server, every
 * incoming client connection gets accepted. On the client side however self-signed certificates are rejected if not
 * present in the custom trust store.
 * Attention: This class acts as a helper for the {@link TLSX509Provider} class and should not be used for a different
 * purpose.
 * Note: Call {@link MHSTrustManager#initialize(File, char[])} before using this trust manager for active connections.
 * @author ChrTopf
 */
public class MHSTrustManager implements X509TrustManager {

    private final TLSX509Provider provider;
    private File trustStoreFile;
    private char[] passphrase;
    private KeyStore trustStore;

    private X509TrustManager defaultTrustManager;
    private X509TrustManager customTrustManager;

    /**
     * Creates a new instance of this class.
     * @param provider The TLS provider to be used in association with this trust manager.
     */
    public MHSTrustManager(TLSX509Provider provider){
        this.provider = provider;
    }

    /**
     * Initializes the trust manager. Attempts to create the trust store file if not present yet.
     * @param trustStoreFile The file of the trust store.
     * @param passphrase The passphrase used to access the trust store.
     * @return returns true if the initialization succeeded and false otherwise.
     */
    public boolean initialize(File trustStoreFile, char[] passphrase){
        this.trustStoreFile = trustStoreFile;
        this.passphrase = passphrase;

        //init the default trust manager factory
        TrustManagerFactory delfaultTMF;
        try{
            delfaultTMF = TrustManagerFactory.getInstance("PKIX");
            // Using null here initialises the TMF with the default trust store.
            delfaultTMF.init((KeyStore) null);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        // get hold of the default trust manager
        for (TrustManager tm : delfaultTMF.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                defaultTrustManager = (X509TrustManager) tm;
                break;
            }
        }

        //check if the trust store file already exists
        if(trustStoreFile.exists()){
            try{
                //load the trust store from disk
                trustStore = provider.loadStore(trustStoreFile, passphrase);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }else{
            try{
                //create empty trust store
                trustStore = KeyStore.getInstance(provider.KEY_STORE_TYPE);
                trustStore.load(null, passphrase);
                //save the store
                provider.saveStore(trustStore, trustStoreFile, passphrase);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        //set the custom trust manager factory with the new trust store
        return setCustomTrustManager(trustStore);
    }

    /**
     * Adds the specified certificate to the trust store file.
     * @param crt The certificate to be trusted.
     * @return returns true on success and false on failure.
     */
    public synchronized boolean trustCertificate(X509Certificate crt){
        //verify that the initialization of this trust manager was successful
        if(defaultTrustManager == null || customTrustManager == null || trustStoreFile == null || passphrase == null || trustStore == null || crt == null){
            return false;
        }
        try {
            //add the certificate to the trust store
            trustStore.setCertificateEntry("crt" + new Date().getTime(), crt);
            //save the trust store
            provider.saveStore(trustStore, trustStoreFile, passphrase);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        //set the custom trust manager factory with the new trust store
        return setCustomTrustManager(trustStore);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        //check if the trust manager has been fully initialized
        if(defaultTrustManager != null && customTrustManager != null){
            return addArrays(defaultTrustManager.getAcceptedIssuers(), customTrustManager.getAcceptedIssuers());
        }
        return null;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        //trust every client
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        //check if the trust manager has been fully initialized
        if(defaultTrustManager != null && customTrustManager != null){
            //check certificate with the default trust manager
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
                return;
            } catch (Exception ignored) {}
            //check with the custom trust manager
            try {
                // This will throw another CertificateException if this fails too.
                customTrustManager.checkServerTrusted(chain, authType);
            }catch (Exception e){
                //execute callback here
                //provider.onCertificateUntrusted(chain);
                //check if the trust store could not contain something
                if(e.getMessage().contains("non-empty")){
                    throw new CertificateException("The trust store is empty, was not found or could not be accessed.");
                }else{
                    throw e;
                }
            }
        }else{
            throw new CertificateException("The trust manager has not been initialized.");
        }
    }

    private <T> T[] addArrays(T[] array1, T[] array2) {
        List<T> resultList = new ArrayList<>(array1.length + array2.length);
        Collections.addAll(resultList, array1);
        Collections.addAll(resultList, array2);

        @SuppressWarnings("unchecked")
        //the type cast is safe as the array1 has the type T[]
        T[] resultArray = (T[]) Array.newInstance(array1.getClass().getComponentType(), 0);
        return resultList.toArray(resultArray);
    }

    private boolean setCustomTrustManager(KeyStore trustStore){
        //initialize the custom trust manager factory with the trust store
        TrustManagerFactory customTMF;
        try{
            customTMF = TrustManagerFactory.getInstance("PKIX");
            customTMF.init(trustStore);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        // Get hold of the default trust manager
        for (TrustManager tm : customTMF.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                customTrustManager = (X509TrustManager) tm;
                break;
            }
        }
        return true;
    }
}
