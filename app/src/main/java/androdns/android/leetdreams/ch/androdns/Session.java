package androdns.android.leetdreams.ch.androdns;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

public class Session implements Serializable {
    public String qname;
    public String server;
    public int qtype;
    public String qclass; //CH, IN, HS
    public String protocol; // DNS, DoT, DoH
    public int port;

    public boolean flag_RD;
    public boolean flag_CD;
    public boolean flag_DO;
    public boolean TCP;

    public String sessionName;
    public String sessionDescription;

    public AnswerScreenState answer;

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append(qname).append("/").append(qclass).append(" @").append(server);
        return builder.toString();
    }

    /**
     * compares question options (not answers)
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Session)){
            return false;
        }
        Session other=(Session)obj;
        return (other.qname.equals(this.qname)
                && other.server.equalsIgnoreCase(this.server)
                && other.qtype == this.qtype
                && other.qclass.equalsIgnoreCase(this.qclass)
                && other.protocol.equalsIgnoreCase(this.protocol)
                && other.flag_RD == this.flag_RD
                && other.flag_CD == this.flag_CD
                && other.flag_DO == this.flag_DO
                && other.TCP == this.TCP
                && other.port == this.port
        );
    }

    public Session(){

    }

    public Session(String server, String qname, int qtype){
        this();
        this.server = server;
        this.qname = qname;
        this.qtype = qtype;
        this.qclass="IN";
        this.protocol="DNS";
        this.flag_RD=true;
        this.flag_CD=false;
        this.flag_DO=false;
        this.TCP = false;
        this.port = 0;
    }

    public Session(String qname, int qtype){
        this("",qname,qtype);
    }

    public Session(String qname, int qtype, String name, String description){
        this(qname,qtype);
        this.sessionName = name;
        this.sessionDescription = description;
    }
    
    public void toJSON(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("qname").value(qname);
        writer.name("server").value(server);
        writer.name("qtype").value(qtype);
        writer.name("qclass").value(qclass);
        writer.name("protocol").value(protocol);
        writer.name("flag_rd").value(flag_RD);
        writer.name("flag_cd").value(flag_CD);
        writer.name("flag_do").value(flag_DO);
        writer.name("tcp").value(TCP);
        writer.name("port").value(port);
        writer.name("sessionName").value(sessionName);
        writer.name("sessionDescription").value(sessionDescription);

        writer.name("answer");
        if(answer != null){
            answer.toJSON(writer);
        } else {
            writer.nullValue();
        }

        writer.endObject();
    }

    public void fromJSON(JSONObject json) throws JSONException {
        qname = json.getString("qname");
        server = json.getString("server");
        qtype = json.getInt("qtype");
        qclass = json.getString("qclass");
        protocol = json.getString("protocol");
        flag_RD = json.getBoolean("flag_rd");
        flag_CD = json.getBoolean("flag_cd");
        flag_DO = json.getBoolean("flag_do");
        TCP = json.getBoolean("tcp");
        try {
            port = json.getInt("port");
        } catch(JSONException e){
            port = 0;
        }
        try {
            JSONObject answerObject = json.getJSONObject("answer");
            AnswerScreenState loadanswer = new AnswerScreenState();
            answer.fromJSON(answerObject);
            this.answer = loadanswer;

        } catch (JSONException e){
            answer = null;
        }

        try{
            sessionName = json.getString("sessionName");
            sessionDescription = json.getString("sessionDescription");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean isDefaultPort(){
        if (this.port==0){
            return true;
        }
        if(this.protocol.equalsIgnoreCase("DNS") && this.port==53){
            return true;
        }
        if(this.protocol.equalsIgnoreCase("DoT") && this.port==853){
            return true;
        }
        return false;
    }
}
