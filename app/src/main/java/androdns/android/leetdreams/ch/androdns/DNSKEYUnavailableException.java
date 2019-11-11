package androdns.android.leetdreams.ch.androdns;

/**
 * Exception returned when the DNSKEY of a domain is unavailable and therefore
 * we can not validate RRSIGs
 */
public class DNSKEYUnavailableException extends Exception {
    public DNSKEYUnavailableException(){
        super();
    }
    public DNSKEYUnavailableException(String s){
        super(s);
    }
}
