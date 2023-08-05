package androdns.android.leetdreams.ch.androdns;

// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

// This is a copy of the SimpleResolver with the bare minimum adaptions for DoT connections
// I first tried to extend from SimpleResolver, but as a few methods and variables are private this did not work out




import android.util.Log;

import org.xbill.DNS.EDNSOption;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.OPTRecord;
import org.xbill.DNS.Opcode;
import org.xbill.DNS.Options;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.ResolverListener;
import org.xbill.DNS.Section;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.Type;
import org.xbill.DNS.WireParseException;
import org.xbill.DNS.ZoneTransferException;
import org.xbill.DNS.ZoneTransferIn;

import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.io.*;
import java.net.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * An implementation of Resolver that sends one query to one server.
 * SimpleResolver handles TCP retries, transaction security (TSIG), and
 * EDNS 0.
 * @see Resolver
 * @see TSIG
 * @see OPTRecord
 *
 * @author Brian Wellington
 */


public class SimpleDoTResolver implements Resolver  {
    protected int CONNECT_READ_TIMEOUT=5000;

    /** The default port to send queries to */
    public static final int DEFAULT_DOT_PORT = 853;

    /** The default EDNS payload size */
    public static final int DEFAULT_EDNS_PAYLOADSIZE = 1280;

    protected InetSocketAddress address;
    private InetSocketAddress localAddress;
    private boolean useTCP, ignoreTruncation;
    private OPTRecord queryOPT;
    private TSIG tsig;
    private Duration timeoutValue;

    private static final short DEFAULT_UDPSIZE = 512;

    private static String defaultResolver = "localhost";
    private static int uniqueID = 0;

    /**
     * Creates a SimpleResolver that will query the specified host
     * @exception UnknownHostException Failure occurred while finding the host
     */
    public
    SimpleDoTResolver(String hostname) throws UnknownHostException {
        this(hostname,DEFAULT_DOT_PORT);
    }

    public SimpleDoTResolver(String hostname, int port)throws UnknownHostException{
        if (hostname == null) {
            hostname = ResolverConfig.getCurrentConfig().server().toString();
            if (hostname == null)
                hostname = defaultResolver;
        }
        InetAddress addr;
        if (hostname.equals("0"))
            addr = InetAddress.getLocalHost();
        else
            addr = InetAddress.getByName(hostname);
        address = new InetSocketAddress(addr, port);
    }

    /**
     * Creates a SimpleResolver.  The host to query is either found by using
     * ResolverConfig, or the default host is used.
     * @see ResolverConfig
     * @exception UnknownHostException Failure occurred while finding the host
     */
    public
    SimpleDoTResolver() throws UnknownHostException {
        this(null);
    }

    /**
     * Gets the destination address associated with this SimpleResolver.
     * Messages sent using this SimpleResolver will be sent to this address.
     * @return The destination address associated with this SimpleResolver.
     */
    public InetSocketAddress
    getAddress() {
        return address;
    }

    /** Sets the default host (initially localhost) to query */
    public static void
    setDefaultResolver(String hostname) {
        defaultResolver = hostname;
    }

    public void
    setPort(int port) {
        address = new InetSocketAddress(address.getAddress(), port);
    }

    /**
     * Sets the address of the server to communicate with.
     * @param addr The address of the DNS server
     */
    public void
    setAddress(InetSocketAddress addr) {
        address = addr;
    }

    /**
     * Sets the address of the server to communicate with (on the default
     * DNS port)
     * @param addr The address of the DNS server
     */
    public void
    setAddress(InetAddress addr) {
        address = new InetSocketAddress(addr, address.getPort());
    }

    /**
     * Sets the local address to bind to when sending messages.
     * @param addr The local address to send messages from.
     */
    public void
    setLocalAddress(InetSocketAddress addr) {
        localAddress = addr;
    }

    /**
     * Sets the local address to bind to when sending messages.  A random port
     * will be used.
     * @param addr The local address to send messages from.
     */
    public void
    setLocalAddress(InetAddress addr) {
        localAddress = new InetSocketAddress(addr, 0);
    }

    public void
    setTCP(boolean flag) {
        this.useTCP = flag;
    }

    public void
    setIgnoreTruncation(boolean flag) {
        this.ignoreTruncation = flag;
    }

    public void
    setEDNS(int level, int payloadSize, int flags, List<EDNSOption> options) {
        if (level != 0 && level != -1)
            throw new IllegalArgumentException("invalid EDNS level - " +
                    "must be 0 or -1");
        if (payloadSize == 0)
            payloadSize = DEFAULT_EDNS_PAYLOADSIZE;
        queryOPT = new OPTRecord(payloadSize, 0, level, flags, options);
    }

    public void
    setEDNS(int level) {
        setEDNS(level, 0, 0, (List<EDNSOption>) null);
    }

    public void
    setTSIGKey(TSIG key) {
        tsig = key;
    }

    TSIG
    getTSIGKey() {
        return tsig;
    }


    private Message
    parseMessage(byte [] b) throws WireParseException {
        try {
            return (new Message(b));
        }
        catch (IOException e) {
            if (Options.check("verbose"))
                e.printStackTrace();
            if (!(e instanceof WireParseException))
                e = new WireParseException("Error parsing message");
            throw (WireParseException) e;
        }
    }

    private void
    verifyTSIG(Message query, Message response, byte [] b, TSIG tsig) {
        if (tsig == null)
            return;
        int error = tsig.verify(response, b, query.getTSIG());
        if (Options.check("verbose"))
            System.err.println("TSIG verify: " + Rcode.TSIGstring(error));
    }

    private void
    applyEDNS(Message query) {
        if (queryOPT == null || query.getOPT() != null)
            return;
        query.addRecord(queryOPT, Section.ADDITIONAL);
    }

    private int
    maxUDPSize(Message query) {
        OPTRecord opt = query.getOPT();
        if (opt == null)
            return DEFAULT_UDPSIZE;
        else
            return opt.getPayloadSize();
    }

    /**
     * Sends a message to a single server and waits for a response.  No checking
     * is done to ensure that the response is associated with the query.
     * @param query The query to send.
     * @return The response.
     * @throws IOException An error occurred while sending or receiving.
     */
    public Message
    send(Message query) throws IOException {
        if (Options.check("verbose"))
            System.err.println("Sending to " +
                    address.getAddress().getHostAddress() +
                    ":" + address.getPort());

        if (query.getHeader().getOpcode() == Opcode.QUERY) {
            Record question = query.getQuestion();
            if (question != null && question.getType() == Type.AXFR)
                return sendAXFR(query);
        }

        query = (Message) query.clone();
        applyEDNS(query);
        if (tsig != null)
            tsig.apply(query, null);

        //
        int udpSize = maxUDPSize(query);



        long endTime = System.currentTimeMillis() + timeoutValue.toMillis();
        byte[] in = sendAndReceive(query);

		/*
		 * Check that the response is long enough.
		 */
            if (in.length < Header.LENGTH) {
                throw new WireParseException("invalid DNS header - " +
                        "too short");
            }
		/*
		 * Check that the response ID matches the query ID.  We want
		 * to check this before actually parsing the message, so that
		 * if there's a malformed response that's not ours, it
		 * doesn't confuse us.
		 */
            int id = ((in[0] & 0xFF) << 8) + (in[1] & 0xFF);
            int qid = query.getHeader().getID();
            if (id != qid) {
                String error = "invalid message id: expected " + qid +
                        "; got id " + id;

                    if (Options.check("verbose")) {
                        System.err.println(error);

                    }
                throw new IOException(error);
            }
            Message response = parseMessage(in);
            verifyTSIG(query, response, in, tsig);
            return response;

    }

    protected byte[] sendAndReceive(Message query) throws IOException{
        byte [] out = query.toWire(Message.MAXLENGTH);
        byte [] in;

        Log.d("dns", "Trying to perform DNS-over-TLS lookup via " + getAddress().toString());
        Socket dnsSocket;

        DatagramPacket outPacket = new DatagramPacket(out, out.length);
        try {

            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, trustAllCerts, null);

            dnsSocket = context.getSocketFactory()
                    .createSocket();


            dnsSocket.connect(address, CONNECT_READ_TIMEOUT);
            dnsSocket.setSoTimeout(CONNECT_READ_TIMEOUT);
            DataOutputStream dos = new DataOutputStream(dnsSocket.getOutputStream());
            byte[] packet = outPacket.getData();
            dos.writeShort(packet.length);
            dos.write(packet);
            dos.flush();

            DataInputStream stream = new DataInputStream(dnsSocket.getInputStream());
            int length = stream.readUnsignedShort();
            in = new byte[length];
            stream.read(in);
            dnsSocket.close();
        } catch(Exception e){
            e.printStackTrace();
            throw new IOException("could not set up DoT connection: "+e.getMessage());
        }
        return in;
    }

    /**
     * Asynchronously sends a message to a single server, registering a listener
     * to receive a callback on success or exception.  Multiple asynchronous
     * lookups can be performed in parallel.  Since the callback may be invoked
     * before the function returns, external synchronization is necessary.
     * @param query The query to send
     * @param listener The object containing the callbacks.
     * @return An identifier, which is also a parameter in the callback
     */
    public Object
    sendAsync(final Message query, final ResolverListener listener) {
        //not implemented
       return null;
    }

    private Message
    sendAXFR(Message query) throws IOException {
        Name qname = query.getQuestion().getName();
        ZoneTransferIn xfrin = ZoneTransferIn.newAXFR(qname, address, tsig);
        xfrin.setTimeout(getTimeout());
        xfrin.setLocalAddress(localAddress);
        try {
            xfrin.run();
        }
        catch (ZoneTransferException e) {
            throw new WireParseException(e.getMessage());
        }
        List records = xfrin.getAXFR();
        Message response = new Message(query.getHeader().getID());
        response.getHeader().setFlag(Flags.AA);
        response.getHeader().setFlag(Flags.QR);
        response.addRecord(query.getQuestion(), Section.QUESTION);
        Iterator it = records.iterator();
        while (it.hasNext())
            response.addRecord((Record)it.next(), Section.ANSWER);
        return response;
    }

    @Override
    public void setTimeout(Duration timeout) {
        timeoutValue = timeout;
    }
}