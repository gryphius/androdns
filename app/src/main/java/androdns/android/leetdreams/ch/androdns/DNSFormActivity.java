package androdns.android.leetdreams.ch.androdns;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import org.xbill.DNS.ResolverListener;
import org.xbill.DNS.Section;
import org.xbill.DNS.SetResponse;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
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
        setAnsTextFromThread("query initializing...");


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

            ansBuffer.append("Lookup qname=");
            ansBuffer.append(qname);
            ansBuffer.append(" qtype=");
            ansBuffer.append(qtype);
            ansBuffer.append("\n");
            setAnsTextFromThread(ansBuffer.toString() + " (initializing)");

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

            Message response = null;


            response = resolver.send(query);
            setAnsTextFromThread(ansBuffer.toString() + " (query sent)");

            int rcode = response.getHeader().getRcode();

            ansBuffer.append("RCODE=");
            ansBuffer.append(Rcode.string(rcode));


            ansBuffer.append("\n");
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


    private void showAnswerFlags(Header header){
        setAnsFlagFromThread(R.id.cbaAA, header.getFlag(Flags.AA));
        setAnsFlagFromThread(R.id.cbaTC, header.getFlag(Flags.TC));
        setAnsFlagFromThread(R.id.cbaRD, header.getFlag(Flags.RD));
        setAnsFlagFromThread(R.id.cbaRA, header.getFlag(Flags.RA));
        setAnsFlagFromThread(R.id.cbaAD, header.getFlag(Flags.AD));
        setAnsFlagFromThread(R.id.cbaCD, header.getFlag(Flags.CD));
        /*
        String[] ansFlags = {"AA", "TC", "RD", "RA", "AD"};
        for (String flag : ansFlags) {
            int flagbit = Flags.value(flag);
            if (response.getHeader().getFlag(flagbit)) {
                ansBuffer.append(" " + flag);
            }
        }
        */
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

    public void queryButtonClicked(View view) {


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
        ((EditText) findViewById(R.id.txtResult)).setText("query started...");

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
