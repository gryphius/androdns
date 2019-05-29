package androdns.android.leetdreams.ch.androdns;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xbill.DNS.Type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;


public class StarredQueries {
    private ArrayList<Session> starredList = new ArrayList<Session>();
    private static final String starredFile = "starred.json";
    private Context context = null;

    public StarredQueries(Context context) {
        this.context = context;
    }

    public void save() {
        try {

            FileOutputStream fos = context.openFileOutput(starredFile, Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, "UTF-8"));
            writer.setIndent("  ");
            writer.beginArray();
            for (Session s : starredList) {
                s.toJSON(writer);
            }
            writer.endArray();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String loadJSONStringFromFile() {
        String json = null;
        try {
            InputStream is = context.openFileInput(starredFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void load() {
        starredList.clear();
        try {
            JSONTokener tokener = new JSONTokener(loadJSONStringFromFile());
            JSONArray jsa = new JSONArray(tokener);
            for (int i = 0; i < jsa.length(); i++) {
                JSONObject obj = jsa.getJSONObject(i);
                Session s = new Session();
                try {
                    s.fromJSON(obj);
                    starredList.add(s);
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(starredList.isEmpty()){
            starredList = getDefaultStarredList();
        }
    }

    public void star(Session s) {
        if(!isStarred(s)) {
            starredList.add(s);
        }
        save();
    }

    public void unstar(Session s) {
        starredList.remove(s);
        save();
    }

    public ArrayList<Session> getStarredList() {
        return starredList;
    }

    public void setStarredList(ArrayList<Session> newlist) {
        starredList = newlist;
    }

    public boolean isStarred(Session s){
        return starredList.contains(s);
    }

    public Session getSessionAt(int position) {
        return getStarredList().get(position);
    }

    public ArrayList<Session> getDefaultStarredList() {

        ArrayList<Session> defaultList = new ArrayList<Session>();

        defaultList.add(new Session("whoami.lua.powerdns.org", Type.TXT));
        defaultList.add(new Session("whoami-ecs.lua.powerdns.org", Type.TXT));
        defaultList.add(new Session("header.lua.powerdns.org", Type.TXT));
        defaultList.add(new Session("latlon.v4.powerdns.org", Type.TXT));
        defaultList.add(new Session("whoami.ds.akahelp.net", Type.TXT));

        return defaultList;

    }

}