package androdns.android.leetdreams.ch.androdns;
import android.util.Log;

import org.xbill.DNS.DNSKEYRecord;
import org.xbill.DNS.DNSSEC;
import org.xbill.DNS.RRSIGRecord;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.util.Hashtable;
import java.util.List;

public class DNSSECVerifier {
    private final Hashtable<String,DNSKEYRecord> knownDNSKeys = new Hashtable<>();
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

    public String verificationStatusString(List<RRset> rrsets){
        StringBuilder buf = new StringBuilder();
        for(RRset rrset:rrsets) {
            for(RRSIGRecord sig:rrset.sigs()){
                buf.append(sig.getFootprint());
                buf.append("/");
                buf.append(sig.getSigner().toString());
                buf.append(":");
                buf.append(Type.string(rrset.getType()));

                buf.append("=");
                try {
                    this.verifySignature(rrset, sig);
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

    public void learnDNSSECKeysFromRRSETs(List<RRset> rrsets){
        DNSKEYRecord dnskey;

        for (RRset rrset:rrsets) {
            if (rrset.getType()!=Type.DNSKEY){
                continue;
            }
            for(Record r:rrset.rrs()){
                dnskey = (DNSKEYRecord) r;
                addDNSKEY(dnskey);
            }


        }

    }
}
