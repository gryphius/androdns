package androdns.android.leetdreams.ch.androdns;

import android.app.Activity;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.OPTRecord;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.ResolverListener;
import org.xbill.DNS.Section;
import org.xbill.DNS.SetResponse;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DNSFormActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "AndroDNS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnsform);
        fillQTypes();

    }

    @Override
    protected void onStart() {
        super.onStart();
        (((Spinner) findViewById(R.id.spinnerKnownTypes))).setOnItemSelectedListener(this);
    }

    public void doLookup() {
        setStatusText("initializing");


        StringBuffer ansBuffer = new StringBuffer();
        try {
            String qname = gettxtQNAMEContent();
            if (!qname.endsWith(".")) {
                qname = qname + ".";
            }
            Name current = Name.fromString(qname);

            int qtype = gettxtQTYPEContent();

            Resolver resolver = null;

            String resolverHostname = gettxtResolverContent().trim();
            setTextViewContent(R.id.txtServerIP, hostToAddr(resolverHostname));
            if (!resolverHostname.equals("")) {
                resolver = new SimpleResolver(resolverHostname);
            } else {
                resolver = new SimpleResolver(null);
            }


            boolean DO_bit = ((CheckBox) findViewById(R.id.cbDO)).isChecked();
            if (DO_bit) {
                resolver.setEDNS(0, 0, Flags.DO, null);

            }

            resolver.setTCP(((CheckBox) findViewById(R.id.cbTCP)).isChecked());

            int query_class = DClass.IN;
            String selectedClass = (((Spinner) findViewById(R.id.spinnerCLASS))).getSelectedItem().toString();
            if (selectedClass.equalsIgnoreCase("ch")) {
                query_class = DClass.CHAOS;
            }
            if (selectedClass.equalsIgnoreCase("hs")) {
                query_class = DClass.HESIOD;
            }

            Record question = Record.newRecord(current, qtype, query_class);
            Message query = Message.newQuery(question);


            boolean RD_bit = ((CheckBox) findViewById(R.id.cbRD)).isChecked();
            //RD bit is set by default
            if (!RD_bit) {
                query.getHeader().unsetFlag(Flags.RD);
            }

            boolean CD_bit = ((CheckBox) findViewById(R.id.cbCD)).isChecked();
            if (CD_bit) {
                query.getHeader().setFlag(Flags.CD);
            }

            int querybytes = query.toWire().length;
            setTextViewContent(R.id.txtQbytes,""+querybytes);

            Message response = null;

            long startTS=System.currentTimeMillis();
            setStatusText("query sent");
            response = resolver.send(query);
            long duration=System.currentTimeMillis()-startTS;
            setStatusText(duration +" ms");

            setTextViewContent(R.id.txtAbytes,""+response.numBytes());


            DecimalFormat df = new DecimalFormat("#.###");
            df.setRoundingMode(RoundingMode.CEILING);
            setTextViewContent(R.id.txtAmpfactor,df.format((float)response.numBytes()/(float)querybytes));


            int rcode = response.getHeader().getRcode();
            setRcodeText(Rcode.string(rcode));


            showAnswerFlags(response.getHeader());
            if (!query.getQuestion().equals(response.getQuestion())) {
                ansBuffer.append("response question section does not match our question.\n");
                ansBuffer.append(response.getQuestion());
                ansBuffer.append("\n");
            }


            ansBuffer.append("ANSWER SECTION:\n");
            ansBuffer.append(rrSetsToString(response.getSectionRRsets(Section.ANSWER)));

            ansBuffer.append("AUTHORITY SECTION:\n");
            ansBuffer.append(rrSetsToString(response.getSectionRRsets(Section.AUTHORITY)));

            ansBuffer.append("ADDITIONAL SECTION:\n");
            ansBuffer.append(rrSetsToString(response.getSectionRRsets(Section.ADDITIONAL)));


        } catch (Exception e) {
            ansBuffer.append(e.toString());
        }

        setAnsTextFromThread(ansBuffer.toString());
    }

    public String hostToAddr(String hostname) {
        if (hostname == null || hostname=="") {
            hostname = ResolverConfig.getCurrentConfig().server();
            if (hostname==null){
                hostname="0";
            }
        }
        InetAddress addr;
        try {
            if (hostname.equals("0"))
                addr = InetAddress.getLocalHost();
            else
                addr = InetAddress.getByName(hostname);
            InetSocketAddress address = new InetSocketAddress(addr, 53);
            return address.getAddress().getHostAddress();
        } catch (UnknownHostException e){

        }
        return "";
    }

    private void showAnswerFlags(Header header){
        setAnsFlagFromThread(R.id.cbaAA, header.getFlag(Flags.AA));
        setAnsFlagFromThread(R.id.cbaTC, header.getFlag(Flags.TC));
        setAnsFlagFromThread(R.id.cbaRD, header.getFlag(Flags.RD));
        setAnsFlagFromThread(R.id.cbaRA, header.getFlag(Flags.RA));
        setAnsFlagFromThread(R.id.cbaAD, header.getFlag(Flags.AD));
        setAnsFlagFromThread(R.id.cbaCD, header.getFlag(Flags.CD));
    }

    public String rrSetsToString(RRset[] rrsets) {
        StringBuffer ansBuffer = new StringBuffer();
        Iterator it;
        int i;

        for (i = 0; i < rrsets.length; i++) {
            RRset rrset = rrsets[i];
            it = rrset.rrs();

            while (it.hasNext()) {
                Record r = (Record) it.next();
                //Log.i(TAG, "rrsetstostring: type=" + r.getType());
                ansBuffer.append(r.toString());
                ansBuffer.append("\n");
            }


            //RRSIGs
            final Iterator<Record> sigIter = rrset.sigs();
            while (sigIter.hasNext()) {
                final Record sigRec = sigIter.next();

                ansBuffer.append(sigRec.toString());
                ansBuffer.append("\n");
            }
        }
        //replace tabs
        String ret = ansBuffer.toString().replace('\t',' ');
        return ret;
    }

    private void setAnsTextFromThread(final String content) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((EditText) findViewById(R.id.txtResult)).setText(content);
            }
        });
    }


    private void setAnsFlagFromThread(final int cbID, boolean checked) {
        final boolean set_checked=checked;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((CheckBox) findViewById(cbID)).setChecked(  set_checked);
            }
        });

    }


    public static void hideKeyboard(Activity activity) {
        if (isKeyboardVisible(activity)) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    public static boolean isKeyboardVisible(Activity activity) {
        ///This method is based on the one described at http://stackoverflow.com/questions/4745988/how-do-i-detect-if-software-keyboard-is-visible-on-android-device
        Rect r = new Rect();
        View contentView = activity.findViewById(android.R.id.content);
        contentView.getWindowVisibleDisplayFrame(r);
        int screenHeight = contentView.getRootView().getHeight();

        int keypadHeight = screenHeight - r.bottom;

        return
                (keypadHeight > screenHeight * 0.15);
    }

    public void queryButtonClicked(View view) {

        hideKeyboard(this);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    doLookup();
                    ;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();


    }

    private void setTextViewContent(final int viewID, final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(viewID)).setText(text);
            }
        });

    }
    private void setStatusText(final String text){
        setTextViewContent(R.id.txtStatusText,text);
    }

    private void setRcodeText(final String text){
        setTextViewContent(R.id.txtRcode,text);
    }

    private void fillQTypes() {
        //
        List<String> spinnerArray = new ArrayList<String>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        for (int i = 1; i <= 32769; i++) {
            String textual = Type.string(i);
            if (textual != null && !textual.startsWith("TYPE")) {
                spinnerArray.add(textual);
            }
        }

        (((Spinner) findViewById(R.id.spinnerKnownTypes))).setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selected = (String) (((Spinner) findViewById(R.id.spinnerKnownTypes))).getSelectedItem();
        String selectedNumber = "" + Type.value(selected);
        (((EditText) findViewById(R.id.txtQTYPE))).setText(selectedNumber);
        //
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String gettxtResolverContent() {
        return getTextFieldContent(R.id.txtServerName);
    }

    public String gettxtQNAMEContent() {
        return getTextFieldContent(R.id.txtQname);
    }

    public int gettxtQTYPEContent() {
        return Integer.valueOf(getTextFieldContent(R.id.txtQTYPE)).intValue();
    }

    private String getTextFieldContent(int txtID) {
        return (((EditText) findViewById(txtID))).getText().toString();
    }
}
