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

/**
 * This class implements the functionality to verify DNSSEC signatures
 * We can only verify RRSIGs for which we have the matching DNSKEYs . AndroDNS does not perform any queries
 * on its own, so these DNSKEYs must be queried manually by the user. They are cached in memory (in this class)
 * and not persisted.
 */
public class DNSSECVerifier {
    // this hashtable caches the known DNSKEY rrsets.  Uses the string "<ownername>-<keytag>" as key
    private Hashtable<String,DNSKEYRecord> knownDNSKeys = new Hashtable<String,DNSKEYRecord>();

    private static final String TAG="DNSSECVerifier";

    /**
     * verifies the RRSIGS of a given RRSET against known DNSKEYs. The matching DNSKEY must have been queried
     * beforehand in the current application setting
     * @param rrset
     * @param rrsig
     * @throws DNSSEC.DNSSECException
     * @throws DNSKEYUnavailableException if the DNSKEY has not been queried in the current application session
     */
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

    /**
     * create the "<ownername>-<keytag>" string to access the DNSKEY hashtable
     * @param dnskey
     * @return
     */
    private String hskey(DNSKEYRecord dnskey){
        return hskey(dnskey.getName().toString(),dnskey.getFootprint());
    }

    /**
     * create the "<ownername>-<keytag>" string to access the DNSKEY hashtable
     * @param ownerName
     * @param keyTag
     * @return
     */
    private String hskey(String ownerName, int keyTag){
        return ownerName.toLowerCase()+"-"+keyTag;
    }

    /**
     * get known DNSKEYrecord from the cache
     * @param ownerName
     * @param keyTag
     * @return
     */
    public DNSKEYRecord getDNSKEY(Name ownerName, int keyTag){

        String hsKey = hskey(ownerName.toString(),keyTag);
        if (!knownDNSKeys.containsKey(hsKey)){
            return null;
        }
        return knownDNSKeys.get(hsKey);

    }

    /**
     * add a DNSKEYrecord to the cache
     * @param dnskey
     */
    public void addDNSKEY(DNSKEYRecord dnskey){
        knownDNSKeys.put(hskey(dnskey),dnskey);
        Log.d(TAG,"learned DNSKEY  "+hskey(dnskey));
    }

    /**
     * build string to display the current verification status to the user
     * @param rrsets
     * @return
     */
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

    /**
     * add all DNSKEY rrs from the rrset to the DNSKEY kcache
     * @param rrsets
     */
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
