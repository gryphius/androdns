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
 * Created by schacher on 27.12.18.
 */

public class SimpleDoHResolver extends SimpleDoTResolver {
    public static final int DEFAULT_DOH_PORT=443;
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

            urlConnection.setDoOutput(true);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(wireformat);
            out.flush();
            InputStream inStream = new BufferedInputStream(urlConnection.getInputStream());
            in = readStream(inStream);
        } catch (Exception e) {
            e.printStackTrace();
           throw new IOException("DoH query failed: "+e.getMessage());
        }
        return in;

    }


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
