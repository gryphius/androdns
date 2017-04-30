package androdns.android.leetdreams.ch.androdns;
import java.io.Serializable;

public class Session implements Serializable {
    public String qname;
    public String server;
    public int qtype;
    public String qclass; //ch, in, hs

    public boolean flag_RD;
    public boolean flag_CD;
    public boolean flag_DO;
    public boolean TCP;

    public long created;
    public long runtimestamp;

    public AnswerScreenState answer;

    public Session(){
        created = System.currentTimeMillis();
    }

    public long duration;
}
