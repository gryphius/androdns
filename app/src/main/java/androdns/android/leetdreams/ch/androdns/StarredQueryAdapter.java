package androdns.android.leetdreams.ch.androdns;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.xbill.DNS.InvalidTypeException;
import org.xbill.DNS.Type;


/**
 * Created by gryphius on 30.04.17.
 */

public class StarredQueryAdapter extends ArrayAdapter<Session> {

    private final Context context;
    private final Session[] values;

    public StarredQueryAdapter(Context context, Session[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.sessiondisplay_starred, parent, false);


        Session session = values[position];

        String qname = session.qname;
        if (!session.server.equals("")){
            qname = qname+"@"+session.server;
        }

        ((TextView) rowView.findViewById(R.id.starred_qname)).setText(qname);

        String type = ""+session.qtype;
        try {
            String txtType = Type.string(session.qtype);
            if (!type.equals(txtType)) {
                type = type + "(" + txtType + ")";
            }
        } catch (InvalidTypeException e) {}
        ((TextView) rowView.findViewById(R.id.starred_qtype)).setText(type);

        StringBuffer flagsBuffer = new StringBuffer();

        // add proto to flags view if not DNS
        if (!session.protocol.equalsIgnoreCase("DNS")){
            flagsBuffer.append(session.protocol);
            flagsBuffer.append(" ");
        }

        // add qclass to flags view if not IN
        if (!session.qclass.equalsIgnoreCase("IN")){
            flagsBuffer.append(session.qclass);
            flagsBuffer.append(" ");
        }

        if (session.flag_RD){
            flagsBuffer.append("RD ");
        }
        if (session.flag_CD){
            flagsBuffer.append("CD ");
        }
        if(session.flag_DO){
            flagsBuffer.append("DO ");
        }
        if(session.TCP && session.protocol.equalsIgnoreCase("DNS")){
            flagsBuffer.append("TCP ");
        }

        ((TextView) rowView.findViewById(R.id.starred_flags)).setText(flagsBuffer.toString());
        return rowView;
    }


}

