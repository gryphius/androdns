package androdns.android.leetdreams.ch.androdns;
import android.util.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by gryphius on 30.04.17.
 */

public class AnswerScreenState implements Serializable {
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
    public long runtimestamp;
    public long duration;

    public AnswerScreenState(){

    }


    public void toJSON(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("status").value(status);
        writer.name("rcode").value(rcode);
        writer.name("server").value(server);
        writer.name("qsize").value(qsize);
        writer.name("asize").value(asize);
        writer.name("flag_AA").value(flag_AA);
        writer.name("flag_TC").value(flag_TC);
        writer.name("flag_RD").value(flag_RD);
        writer.name("flag_RA").value(flag_RA);
        writer.name("flag_AD").value(flag_AD);
        writer.name("flag_CD").value(flag_CD);
        writer.name("answerText").value(answerText);
        writer.name("runtimestamp").value(runtimestamp);
        writer.name("duration").value(duration);
        writer.endObject();
    }

    public void fromJSON(JSONObject json) throws JSONException {
        status = json.getString("status");
        rcode = json.getInt("rcode");
        server = json.getString("server");
        qsize = json.getInt("qsize");
        asize = json.getInt("asize");
        flag_AA = json.getBoolean("flag_AA");
        flag_TC = json.getBoolean("flag_TC");
        flag_RD = json.getBoolean("flag_RD");
        flag_RA = json.getBoolean("flag_RA");
        flag_AD = json.getBoolean("flag_AD");
        flag_CD = json.getBoolean("flag_CD");
        answerText = json.getString("answerText");
        runtimestamp = json.getLong("runtimestamp");
        duration = json.getLong("duration");
    }
}
