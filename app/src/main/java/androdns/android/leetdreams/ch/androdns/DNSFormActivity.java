package androdns.android.leetdreams.ch.androdns;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;


import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.InvalidTypeException;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.IDN;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DNSFormActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnFocusChangeListener {
    private static final String TAG = "AndroDNS";
    private Session activeSession = null;
    private History history;
    private StarredQueries starred;
    private DNSSECVerifier dnssecVerifier=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnsform);
        fillQTypes();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        history = new History(getApplicationContext());
        history.load();

        starred = new StarredQueries(getApplicationContext());
        starred.load();
    }

    @Override
    protected void onStart() {
        super.onStart();
        (((Spinner) findViewById(R.id.spinnerKnownTypes))).setOnItemSelectedListener(this);
        (((Spinner) findViewById(R.id.spinnerProto))).setOnItemSelectedListener(this);
        (((EditText) findViewById(R.id.txtQTYPE))).setOnFocusChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        return true;
    }

    public DNSSECVerifier getDnssecVerifier(){
        if (dnssecVerifier==null){
            dnssecVerifier=new DNSSECVerifier();
        }
        return dnssecVerifier;
    }

    /**
     * update the the lower gui section with the Values from an AnswerScreenState
     * @param state
     * history: if true, do not update the title
     */
    public void updateScreenState(final AnswerScreenState state, final boolean history){
        Runnable guiUpdate = new Runnable() {
            @Override
            public void run() {
                if (!history){
                    setTitle("");
                }

                ((TextView) findViewById(R.id.txtStatusText)).setText(state.status);
                ((TextView) findViewById(R.id.txtServerIP)).setText(state.server);
                ((TextView) findViewById(R.id.txtQbytes)).setText(""+state.qsize);
                ((TextView) findViewById(R.id.txtAbytes)).setText(""+state.asize);
                ((EditText) findViewById(R.id.txtResult)).setText(state.answerText);
                if (state.rcode>-1) {
                    ((TextView) findViewById(R.id.txtRcode)).setText(Rcode.string(state.rcode));
                } else {
                    ((TextView) findViewById(R.id.txtRcode)).setText("");

                }


                ((CheckBox) findViewById(R.id.cbaAA)).setChecked(  state.flag_AA);
                ((CheckBox) findViewById(R.id.cbaTC)).setChecked(  state.flag_TC);
                ((CheckBox) findViewById(R.id.cbaRD)).setChecked(  state.flag_RD);
                ((CheckBox) findViewById(R.id.cbaRA)).setChecked(  state.flag_RA);
                ((CheckBox) findViewById(R.id.cbaAD)).setChecked(  state.flag_AD);
                ((CheckBox) findViewById(R.id.cbaCD)).setChecked(  state.flag_CD);
            }
        };

        runOnUiThread(guiUpdate);
    }

    /**
     * set the screen state from a history entry
     * @param session
     */
    public void setScreenState(final Session session){
        activeSession = session;

        Runnable guiUpdate = new Runnable() {
            @Override
            public void run() {
                long runts = session.runtimestamp;
                if (runts>0) {
                    setTitle(HistoryAdapter.getDate(session.runtimestamp, "yyyy-MM-dd hh:mm:ss"));
                } else {
                    setTitle("");
                }
                ((EditText) findViewById(R.id.txtQname)).setText(session.qname);
                ((EditText) findViewById(R.id.txtServerName)).setText(session.server);
                ((EditText) findViewById(R.id.txtQTYPE)).setText(""+session.qtype);
                Spinner qtypespinner = (Spinner) findViewById(R.id.spinnerKnownTypes);
                try {
                    qtypespinner.setSelection(getIndex(qtypespinner, Type.string(session.qtype)));
                } catch (Exception e){} //invalid type

                Spinner classSpinner = ( Spinner)findViewById(R.id.spinnerCLASS);
                try{
                    classSpinner.setSelection(getIndex(classSpinner,session.qclass));
                }catch (Exception e){} //invalid class

                Spinner protoSpinner = ( Spinner)findViewById(R.id.spinnerProto);
                try{
                    protoSpinner.setSelection(getIndex(protoSpinner,session.protocol));
                }catch (Exception e){} //invalid class

                ((CheckBox) findViewById(R.id.cbTCP)).setChecked(session.TCP);
                ((CheckBox) findViewById(R.id.cbCD)).setChecked(session.flag_CD);
                ((CheckBox) findViewById(R.id.cbRD)).setChecked(session.flag_RD);
                ((CheckBox) findViewById(R.id.cbDO)).setChecked(session.flag_DO);


            }
        };

        runOnUiThread(guiUpdate);

        if (session.answer!=null){
            updateScreenState(session.answer,true);
        }
        updateStarredImageState();
    }

    /**
     * update answer screen state if the session this comes from is still the active one*
     **/
    public void updateStreenStateIfCurrent(Session session, AnswerScreenState state){
        if (session == activeSession){
            updateScreenState(state,false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                Intent historyIntent = new Intent(this, HistoryActivity.class);
                startActivityForResult(historyIntent,1);
                return true;

            case R.id.action_star:
                Intent starIntent = new Intent(this, StarredQueriesActivity.class);
                startActivityForResult(starIntent,1);

                return true;

            case R.id.action_help:
                showHelp();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    int returnValue = data.getIntExtra("entry",0);
                    String source = data.getStringExtra("source");
                    if(source == null){
                        source="history";
                    }
                    switch (source){
                        case "starred":
                            clearAnswer();
                            setScreenState(starred.getSessionAt(returnValue));
                            break;
                        default: //"history
                            setScreenState(history.getSessionAt(returnValue));
                    }

                }
                break;
            }
        }
    }

    /**
     * perform the lookup, store the answer in an answerscreenstate and update the screen when done
     * @param session
     */
    public void doLookup(Session session){
        activeSession = session;
        session.runtimestamp = System.currentTimeMillis();
        AnswerScreenState answerState = new AnswerScreenState();
        String answerOutput="";

        try {
            // Set up the query
            String qname = session.qname;

            // IDNA: convert to ACE String
            qname=IDN.toASCII(qname);

            if (!qname.endsWith(".")) {
                qname = qname + ".";
            }
            Name current = Name.fromString(qname);
            int qtype = session.qtype;

            StringBuffer ansBuffer = new StringBuffer();

            Resolver resolver = null;

            String resolverHostname = session.server;


            String hostnameArg = IDN.toASCII(resolverHostname);
            if (hostnameArg.trim().equals("")){
                DnsServersDetector detector = new DnsServersDetector(this);
                String[] dnsServers=detector.getServers();
                if(dnsServers.length>0){
                    hostnameArg=dnsServers[0];
                } else {
                    hostnameArg=null;
                }
                Log.d(TAG,"Auto detected DNS Server: "+hostnameArg);
            }

            answerState.server = hostToAddr(hostnameArg);

            //update the server ip in the gui before the query, so we see it while we try to connect
            setTextViewContent(R.id.txtServerIP,answerState.server);

            if (session.protocol.equalsIgnoreCase("DoT")){
                resolver = new SimpleDoTResolver(hostnameArg);
            } else if(session.protocol.equalsIgnoreCase("DoH")){
                resolver = new SimpleDoHResolver(hostnameArg);
            } else {
                resolver = new SimpleResolver(hostnameArg);
            }

            if (session.flag_DO) {
                resolver.setEDNS(0, 0, Flags.DO, null);
            }

            resolver.setTCP(session.TCP);

            int query_class = DClass.IN;
            String selectedClass = session.qclass;
            if (selectedClass.equalsIgnoreCase("ch")) {
                query_class = DClass.CHAOS;
            }
            if (selectedClass.equalsIgnoreCase("hs")) {
                query_class = DClass.HESIOD;
            }

            Record question_record = Record.newRecord(current, qtype, query_class);
            Message query = Message.newQuery(question_record);


            //RD bit is set by default
            if (!session.flag_RD) {
                query.getHeader().unsetFlag(Flags.RD);
            }

            if (session.flag_CD) {
                query.getHeader().setFlag(Flags.CD);
            }

            int querybytes = query.toWire().length;
            answerState.qsize = querybytes;


            // Query ready, send it
            Message response = null;
            long startTS=System.currentTimeMillis();
            setStatusText("query sent");
            response = resolver.send(query);

            if (activeSession !=session){
                return; // this query has been aborted/overwritten by a new one
            }

            long duration=System.currentTimeMillis()-startTS;
            session.duration = duration;
            answerState.status = duration +" ms";
            answerState.asize = response.numBytes();

            int rcode = response.getHeader().getRcode();
            answerState.rcode = rcode;


            setAnswerFlagsToState(response.getHeader(), answerState);

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

            // DNSSSEC validation
            DNSSECVerifier verifier = getDnssecVerifier();
            verifier.learnDNSSECKeysFromRRSETs(response.getSectionRRsets(Section.ANSWER));

            if(session.flag_DO) {
                ansBuffer.append("\nvalidation status :\n");
                ansBuffer.append(verifier.verificationStatusString(response.getSectionRRsets(Section.ANSWER)));
                ansBuffer.append(verifier.verificationStatusString(response.getSectionRRsets(Section.AUTHORITY)));
                ansBuffer.append("\n");
            }


            answerOutput = ansBuffer.toString();
        } catch (TextParseException e) {
            if (activeSession == session) {
                answerOutput="Invalid qname";
                answerState.status = "INVALID";
            }

        } catch (UnknownHostException e){
            if (activeSession == session) {
                answerOutput="Host not found: " + e.toString();
                answerState.status = "ERROR";
            }

        } catch (java.net.SocketTimeoutException e){
            if (activeSession == session) {
                answerOutput="Query timed out";
                answerState.status = "TIMEOUT";
            }
        } catch (IOException e){
            if (activeSession == session) {
                answerOutput="I/O Error: " + e.toString();
                answerState.status = "ERROR";
            }
        }  catch (InvalidTypeException e){
            answerOutput="Invalid type";
            answerState.status = "INVALID";
        }
        session.answer = answerState;
        answerState.answerText = answerOutput;

        history.addEntry(session);
        updateStarredImageState();
        updateStreenStateIfCurrent(session,answerState);
    }

    public void clearAnswer(){
        updateScreenState(new AnswerScreenState(), false);
    }

    public Session sessionFromScreenState(){
        Session screenSession = new Session();

        //build the question object
        String qname = gettxtQNAMEContent();
        screenSession.qname = qname;
        screenSession.qtype = gettxtQTYPEContent();
        screenSession.flag_RD = ((CheckBox) findViewById(R.id.cbRD)).isChecked();
        screenSession.flag_CD = ((CheckBox) findViewById(R.id.cbCD)).isChecked();
        screenSession.flag_DO = ((CheckBox) findViewById(R.id.cbDO)).isChecked();
        screenSession.qclass = (((Spinner) findViewById(R.id.spinnerCLASS))).getSelectedItem().toString();
        screenSession.server = gettxtResolverContent().trim();
        screenSession.TCP = ((CheckBox) findViewById(R.id.cbTCP)).isChecked();
        screenSession.protocol = (((Spinner) findViewById(R.id.spinnerProto))).getSelectedItem().toString();
        return screenSession;
    }

    public void doLookup() {
        setStatusText("initializing");
        Session thisQuestion = sessionFromScreenState();
        doLookup(thisQuestion);
    }


    public void updateStarredImageState(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageButton btn = (ImageButton)findViewById(R.id.btnStar);
                Session sess = sessionFromScreenState();
                if (starred.isStarred(sess)){
                    btn.setImageResource(R.drawable.starred);
                } else {
                    btn.setImageResource(R.drawable.notstarred);
                }

            }
        });
    }

    public void starUnstar(View view){
        final Session screenSession = sessionFromScreenState();
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        final boolean currentlyStarred = starred.isStarred(screenSession);

        adb.setTitle("Star current query?");
        if (currentlyStarred){
            adb.setTitle("Unstar current query?");
        }
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!currentlyStarred){
                            starred.star(screenSession);
                        } else {
                            starred.unstar(screenSession);
                        }
                        updateStarredImageState();
                    }
                });
                adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        adb.show();
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

    private void setAnswerFlagsToState(Header header, AnswerScreenState state){
        state.flag_AA =  header.getFlag(Flags.AA);
        state.flag_AD = header.getFlag(Flags.AD);
        state.flag_TC = header.getFlag(Flags.TC);
        state.flag_RD = header.getFlag(Flags.RD);
        state.flag_RA = header.getFlag(Flags.RA);
        state.flag_CD = header.getFlag(Flags.CD);
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
        clearAnswer();
        updateStarredImageState();
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
        Collections.sort(spinnerArray);
        (((Spinner) findViewById(R.id.spinnerKnownTypes))).setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spProtocol = (Spinner) findViewById(R.id.spinnerProto);
        if (parent==spProtocol){

            String proto = (String) spProtocol.getSelectedItem();

            CheckBox tcpCheckbox = (CheckBox) findViewById(R.id.cbTCP);
            if (!proto.equalsIgnoreCase("DNS")){ //DoH and DoT are always TCP
                tcpCheckbox.setEnabled(false);
            } else {
                tcpCheckbox.setEnabled(true);
            }
        }

        // set qtype number from spinner
        Spinner spKnownTypes = (Spinner) findViewById(R.id.spinnerKnownTypes);
        if (parent==spKnownTypes) {
            String selected = (String) spKnownTypes.getSelectedItem();
            String selectedNumber = "" + Type.value(selected);
            (((EditText) findViewById(R.id.txtQTYPE))).setText(selectedNumber);
        }

        updateStarredImageState();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {

        EditText txtQtype = (((EditText) findViewById(R.id.txtQTYPE)));
        Spinner spKnownTypes = (Spinner) findViewById(R.id.spinnerKnownTypes);

        // set spinner qtype from number
        if(view==txtQtype && !hasFocus){
            try{
                int qtype = gettxtQTYPEContent();
                String qnameString = Type.string(qtype);
                int ind = getIndex(spKnownTypes,qnameString,-1);
                if (ind>-1){
                    spKnownTypes.setSelection(ind);
                }
            }catch (Exception e){}
        }

        updateStarredImageState();

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

    private int getIndex(Spinner spinner, String myString)
    {
        return getIndex(spinner,myString,0);
    }

    private int getIndex(Spinner spinner, String myString, int notFoundDefault)
    {
        int index = notFoundDefault;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }



    public void showHelp(){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.help, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(findViewById(R.id.scrollViewMain), Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

    }
}
