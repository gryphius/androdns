package androdns.android.leetdreams.ch.androdns;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
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

public class HistoryAdapter extends ArrayAdapter<Session> {

    private final Context context;
    private final Session[] values;

    public HistoryAdapter(Context context, Session[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.sessiondisplay, parent, false);


        Session session = values[position];

        TextView dateView = (TextView) rowView.findViewById(R.id.history_entry_date);
        dateView.setText(getDate(session.runtimestamp, "yyyy-MM-dd hh:mm:ss"));

        String qname = session.qname;
        if (!session.server.equals("")){
            qname = qname+"@"+session.server;
        }

        ((TextView) rowView.findViewById(R.id.history_qname)).setText(qname);

        String type = ""+session.qtype;
        try {
            String txtType = Type.string(session.qtype);
            if (!type.equals(txtType)) {
                type = type + "(" + txtType + ")";
            }
        } catch (InvalidTypeException e) {}
        ((TextView) rowView.findViewById(R.id.history_qtype)).setText(type);

        StringBuffer flagsBuffer = new StringBuffer();
        if (session.flag_RD){
            flagsBuffer.append("RD ");
        }
        if (session.flag_CD){
            flagsBuffer.append("CD ");
        }
        if(session.flag_DO){
            flagsBuffer.append("DO ");
        }
        if(session.TCP){
            flagsBuffer.append("TCP ");
        }

        ((TextView) rowView.findViewById(R.id.history_flags)).setText(flagsBuffer.toString());
        return rowView;
    }


    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}

