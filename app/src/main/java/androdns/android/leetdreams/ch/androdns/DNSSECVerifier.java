package androdns.android.leetdreams.ch.androdns;
import android.util.Log;

import org.xbill.DNS.DNSKEYRecord;
import org.xbill.DNS.DNSSEC;
import org.xbill.DNS.RRSIGRecord;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class DNSSECVerifier {
    private Hashtable<String,DNSKEYRecord> knownDNSKeys = new Hashtable<String,DNSKEYRecord>();
    private static final String TAG="DNSSECVerifier";

    public void verifySignature(RRset rrset, RRSIGRecord rrsig) throws DNSSEC.DNSSECException,DNSKEYUnavailableException {
        Name ownerName = rrsig.getSigner();
        int keyTag = rrsig.getFootprint();

        DNSKEYRecord dnskey=getDNSKEY(ownerName,keyTag);
        if(dnskey==null){
            Log.d(TAG,"missing DNSKEY "+hskey(ownerName.toString(),keyTag));
            throw new DNSKEYUnavailableException("DNSKEY "+ownerName+"/"+keyTag+" not available");
        }

        DNSSEC.verify(rrset, rrsig, dnskey);

    }

    private String hskey(DNSKEYRecord dnskey){
        return hskey(dnskey.getName().toString(),dnskey.getFootprint());
    }
    private String hskey(String ownerName, int keyTag){
        return ownerName.toLowerCase()+"-"+keyTag;
    }

    public DNSKEYRecord getDNSKEY(Name ownerName, int keyTag){

        String hsKey = hskey(ownerName.toString(),keyTag);
        if (!knownDNSKeys.containsKey(hsKey)){
            return null;
        }
        return knownDNSKeys.get(hsKey);

    }

    public void addDNSKEY(DNSKEYRecord dnskey){
        knownDNSKeys.put(hskey(dnskey),dnskey);
        Log.d(TAG,"learned DNSKEY  "+hskey(dnskey));
    }

    public String verificationStatusString(RRset[] rrsets){
        StringBuffer buf = new StringBuffer();
        for(RRset rrset:rrsets) {
            Iterator<RRSIGRecord> sigs = rrset.sigs();

            while (sigs.hasNext()) {
                RRSIGRecord rrsig = sigs.next();
                int keyID = rrsig.getFootprint();
                buf.append(keyID);
                buf.append("/");
                buf.append(rrsig.getSigner().toString());
                buf.append(":");
                buf.append(Type.string(rrset.getType()));

                buf.append("=");
                try {
                    this.verifySignature(rrset, rrsig);
                    buf.append("verified");
                } catch (DNSKEYUnavailableException dku) {
                    buf.append("have to learn DNSKEY");
                } catch (DNSSEC.DNSSECException dse) {
                    if (dse instanceof DNSSEC.SignatureExpiredException) {
                        buf.append("expired");
                    } else if (dse instanceof DNSSEC.SignatureNotYetValidException) {
                        buf.append("not yet valid");
                    } else if (dse instanceof DNSSEC.UnsupportedAlgorithmException) {
                        buf.append("unsupported alg");
                    } else {
                        buf.append(dse.getMessage());
                    }
                }
                buf.append("\n");
            }
        }
        String validationStatus = buf.toString();
        Log.d(TAG,"Validation status: "+validationStatus);
        return validationStatus;
    }

    public void learnDNSSECKeysFromRRSETs(RRset[] rrsets){
        Iterator it;
        int i;

        for (i = 0; i < rrsets.length; i++) {
            RRset rrset = rrsets[i];
            if (rrset.getType()!=Type.DNSKEY){
                continue;
            }
            it = rrset.rrs();

            while (it.hasNext()) {
                DNSKEYRecord r = (DNSKEYRecord) it.next();
                addDNSKEY(r);
            }


        }

    }
}
