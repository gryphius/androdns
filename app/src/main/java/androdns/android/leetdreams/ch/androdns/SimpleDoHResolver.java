package androdns.android.leetdreams.ch.androdns;

import android.util.Log;

import org.xbill.DNS.Message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Simple DNS-over-HTTPS resolver, using the HTTP POST method
 * https://tools.ietf.org/html/rfc8484
 */

public class SimpleDoHResolver extends SimpleDoTResolver {
    public static final int DEFAULT_DOH_PORT=443;

    // holds the URL where we send the DOH query to
    protected String url = null;

    /**
     * Creates a SimpleResolver that will query the specified host
     * @exception UnknownHostException Failure occurred while finding the host
     */
    public
    SimpleDoHResolver(String url) throws UnknownHostException {
        if (!url.toLowerCase().startsWith("http")){
            url="https://"+url;
        }
        trustAllCertificates();
        this.url=url;
    }


    /**
     * send the dns message as a post request to the DOH endpoint
     * returns a byte array containing the answer
     * @param query
     * @return
     * @throws IOException
     */
    protected byte[] sendAndReceive(Message query) throws IOException {
        byte [] wireformat = query.toWire(Message.MAXLENGTH);
        byte [] in;

        Log.d("dns", "Trying to perform DNS-over-HTTPS lookup via " + url);

        try {
            URL url = new URL(this.url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(CONNECT_READ_TIMEOUT);
            urlConnection.setReadTimeout(CONNECT_READ_TIMEOUT);
            urlConnection.setRequestProperty("accept", "application/dns-message");
            urlConnection.setRequestProperty("content-type", "application/dns-message");
            // disable default user-agent header, we don't want to send private info to the server
            // apparently it's not possible to remove a header from a urlconnection, but at least we
            // can set it to the empty string
            urlConnection.setRequestProperty("User-Agent","");

            urlConnection.setDoOutput(true);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(wireformat);
            out.flush();
            InputStream inStream = new BufferedInputStream(urlConnection.getInputStream());
            in = readStream(inStream);
        } catch (Exception e) {
            e.printStackTrace();
           throw new IOException("DoH query failed: "+e.toString());
        }
        return in;

    }

    /**
     * read the remainder from inputstream into a bytearray
     * @param is
     * @return
     */
    private byte[] readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * AndroDNS is a debugging tool which should also allow querying a server without a trusted/valid certificate
     * This method disables certificate verification
     */
    public void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }
}
