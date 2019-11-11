package androdns.android.leetdreams.ch.androdns;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;

import java.io.Serializable;

/**
 * The AnswerscreenState Object holds all required variables to build the screen state in the response section,
 * i.e. all returned flags, answer section, status etc
 */

public class AnswerScreenState implements Serializable {
    public long timestamp = System.currentTimeMillis();
    public String status="";
    public int rcode=-1;
    public String server="";

    public int qsize=0;
    public int asize=0;

    public boolean flag_AA=false;
    public boolean flag_TC=false;
    public boolean flag_RD=false;
    public boolean flag_RA=false;
    public boolean flag_AD=false;
    public boolean flag_CD=false;

    public String answerText ="";

    public AnswerScreenState(){

    }

    /**
     * set the flags from a DNS message header
     * @param header
     */
    public void setFlagsFromMessageHeader(Header header) {
        flag_AA = header.getFlag(Flags.AA);
        flag_AD = header.getFlag(Flags.AD);
        flag_TC = header.getFlag(Flags.TC);
        flag_RD = header.getFlag(Flags.RD);
        flag_RA = header.getFlag(Flags.RA);
        flag_CD = header.getFlag(Flags.CD);
    }
}
