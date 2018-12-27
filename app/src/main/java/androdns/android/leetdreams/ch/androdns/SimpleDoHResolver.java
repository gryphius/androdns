package androdns.android.leetdreams.ch.androdns;

import android.util.Log;

import org.xbill.DNS.Message;
import org.xbill.DNS.ResolverConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
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

        this.url=url;
    }



    protected byte[] sendAndReceive(Message query) throws IOException {
        byte [] wireformat = query.toWire(Message.MAXLENGTH);
        byte [] in;

        Log.d("dns", "Trying to perform DNS-over-HTTPS lookup via " + url);

        try {
            URL url = new URL(this.url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
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
}
