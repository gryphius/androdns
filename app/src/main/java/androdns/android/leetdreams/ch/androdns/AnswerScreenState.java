package androdns.android.leetdreams.ch.androdns;
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
}
