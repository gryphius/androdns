package androdns.android.leetdreams.ch.androdns;

/**
 * Created by gryphius on 29.04.17.
 */

public class Question {
    public String qname;
    public String server;
    public int qtype;
    public String qclass; //ch, in, hs

    public boolean flag_RD;
    public boolean flag_CD;
    public boolean flag_DO;
    public boolean TCP;

    public long created;

    public Question(){
        created = System.currentTimeMillis();
    }
}
