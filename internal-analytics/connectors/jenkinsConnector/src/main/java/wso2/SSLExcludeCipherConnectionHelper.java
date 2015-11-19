package wso2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

/**
 * Created by rajith on 10/20/15.
 */
public class SSLExcludeCipherConnectionHelper {
    private Logger logger = Logger.getLogger(SSLExcludeCipherConnectionHelper.class);

    private String[] exludedCipherSuites = {"_DHE_","_DH_"};

//    private String trustCert = null;

    private TrustManagerFactory tmf;

    public void setExludedCipherSuites(String[] exludedCipherSuites) {
        this.exludedCipherSuites = exludedCipherSuites;
    }

//    public SSLExcludeCipherConnectionHelper(String trustCert) {
//        super();
//        this.trustCert = trustCert;
//        //Security.addProvider(new BouncyCastleProvider());
//        try {
//            this.initTrustManager();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    public SSLExcludeCipherConnectionHelper(InputStream stream) {
        super();
//        this.trustCert = trustCert;
        //Security.addProvider(new BouncyCastleProvider());
        try {
            this.initTrustManager(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initTrustManager(InputStream stream) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = new BufferedInputStream(stream);
        Certificate ca = null;
        try {
            ca = cf.generateCertificate(caInput);
            logger.debug("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);
    }

    public String get(URL url) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        SSLParameters params = context.getSupportedSSLParameters();
        List<String> enabledCiphers = new ArrayList<String>();
        for (String cipher : params.getCipherSuites()) {
            boolean exclude = false;
            if (exludedCipherSuites != null) {
                for (int i=0; i<exludedCipherSuites.length && !exclude; i++) {
                    exclude = cipher.indexOf(exludedCipherSuites[i]) >= 0;
                }
            }
            if (!exclude) {
                enabledCiphers.add(cipher);
            }
        }
        String[] cArray = new String[enabledCiphers.size()];
        enabledCiphers.toArray(cArray);

        // Tell the URLConnection to use a SocketFactory from our SSLContext
        HttpsURLConnection urlConnection =
                (HttpsURLConnection)url.openConnection();
        SSLSocketFactory sf = context.getSocketFactory();
        sf = new DOSSLSocketFactory(sf, cArray);
        urlConnection.setSSLSocketFactory(sf);
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String inputLine;
        StringBuffer buffer = new StringBuffer();
        while ((inputLine = in.readLine()) != null)
            buffer.append(inputLine);
        in.close();

        return buffer.toString();
    }

    private class DOSSLSocketFactory extends javax.net.ssl.SSLSocketFactory {

        private SSLSocketFactory sf = null;
        private String[] enabledCiphers = null;

        private DOSSLSocketFactory(SSLSocketFactory sf, String[] enabledCiphers) {
            super();
            this.sf = sf;
            this.enabledCiphers = enabledCiphers;
        }

        private Socket getSocketWithEnabledCiphers(Socket socket) {
            if (enabledCiphers != null && socket != null && socket instanceof SSLSocket)
                ((SSLSocket)socket).setEnabledCipherSuites(enabledCiphers);

            return socket;
        }

        @Override
        public Socket createSocket(Socket s, String host, int port,
                                   boolean autoClose) throws IOException {
            return getSocketWithEnabledCiphers(sf.createSocket(s, host, port, autoClose));
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return sf.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            if (enabledCiphers == null)
                return sf.getSupportedCipherSuites();
            else
                return enabledCiphers;
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException,
                                                                 UnknownHostException {
            return getSocketWithEnabledCiphers(sf.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port)
                throws IOException {
            return getSocketWithEnabledCiphers(sf.createSocket(address, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localAddress,
                                   int localPort) throws IOException, UnknownHostException {
            return getSocketWithEnabledCiphers(sf.createSocket(host, port, localAddress, localPort));
        }

        @Override
        public Socket createSocket(InetAddress address, int port,
                                   InetAddress localaddress, int localport) throws IOException {
            return getSocketWithEnabledCiphers(sf.createSocket(address, port, localaddress, localport));
        }

    }
}
