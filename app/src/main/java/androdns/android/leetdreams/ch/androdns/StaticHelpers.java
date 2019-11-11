package androdns.android.leetdreams.ch.androdns;

import org.xbill.DNS.RRset;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

public class StaticHelpers {
    /**
     * get address from hostname
     * @param hostname
     * @return
     */
    public static String hostToAddr(String hostname) {
        if (hostname == null || hostname == "") {
            hostname = ResolverConfig.getCurrentConfig().server();
            if (hostname == null) {
                hostname = "0";
            }
        }
        InetAddress addr;
        try {
            if (hostname.equals("0"))
                addr = InetAddress.getLocalHost();
            else
                addr = InetAddress.getByName(hostname);
            InetSocketAddress address = new InetSocketAddress(addr, 53);
            return address.getAddress().getHostAddress();
        } catch (UnknownHostException e) {

        }
        return "";
    }

    /**
     * Transform RRSets into a string to provide dig-like output
     * @param rrsets
     * @return
     */
    public static String rrSetsToString(RRset[] rrsets) {
        StringBuffer ansBuffer = new StringBuffer();
        Iterator it;
        int i;

        for (i = 0; i < rrsets.length; i++) {
            RRset rrset = rrsets[i];
            it = rrset.rrs();

            while (it.hasNext()) {
                Record r = (Record) it.next();
                //Log.i(TAG, "rrsetstostring: type=" + r.getType());
                ansBuffer.append(r.toString());
                ansBuffer.append("\n");
            }

            //RRSIGs
            final Iterator<Record> sigIter = rrset.sigs();
            while (sigIter.hasNext()) {
                final Record sigRec = sigIter.next();

                ansBuffer.append(sigRec.toString());
                ansBuffer.append("\n");
            }
        }
        //replace tabs
        String ret = ansBuffer.toString().replace('\t', ' ');
        return ret;
    }
}
