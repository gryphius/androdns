package androdns.android.leetdreams.ch.androdns;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a query and response. All question related variables are directly available in this object
 * All variables related to the response are available in a referenced AnswerScreenState object
 */
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

    public long created;
    public long runtimestamp;

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
        created = System.currentTimeMillis();
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

    public long duration;

    /**
     * convert the current session (question part only) into a JSON object
     * @param writer
     * @throws IOException
     */
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
        writer.endObject();
    }

    /**
     * initialize this object from a JSONobject
     * @param json
     * @throws JSONException
     */
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

    }

    /**
     * helper method, returns true if the port used in this query is the default
     * for the given protocol, i.e. 53 for plain old DNS, 853 for DoT
     * Note, we do not handle DoH here, for DoH the port is directly in the query url, i.e. servername
     * @return
     */
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
