package androdns.android.leetdreams.ch.androdns;

import android.content.Context;
import android.util.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xbill.DNS.Type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class BookmarkedQueries {
    private ArrayList<Session> bookmarks = new ArrayList<Session>();
    private static final String bookmarkFile = "bookmarks.json";
    private Context context = null;

    public BookmarkedQueries(Context context) {
        this.context = context;
    }

    public void save() {
        try {

            FileOutputStream fos = context.openFileOutput(bookmarkFile, Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, "UTF-8"));
            writer.setIndent("  ");
            writer.beginArray();
            for (Session s : bookmarks) {
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
            InputStream is = context.openFileInput(bookmarkFile);
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
        bookmarks.clear();
        try {
            JSONTokener tokener = new JSONTokener(loadJSONStringFromFile());
            JSONArray jsa = new JSONArray(tokener);
            for (int i = 0; i < jsa.length(); i++) {
                JSONObject obj = jsa.getJSONObject(i);
                Session s = new Session();
                try {
                    s.fromJSON(obj);
                    bookmarks.add(s);
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(bookmarks.isEmpty()){
            bookmarks = getDefaultBookmarks();
        }
    }

    public void bookmark(Session s) {
        if(!isBookmarked(s)) {
            bookmarks.add(s);
        }
        save();
    }

    public void removeBookmark(Session s) {
        bookmarks.remove(s);
        save();
    }

    public ArrayList<Session> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(ArrayList<Session> newBookmarks) {
        bookmarks = newBookmarks;
    }

    public boolean isBookmarked(Session s){
        return bookmarks.contains(s);
    }

    public Session getSessionAt(int position) {
        return getBookmarks().get(position);
    }

    public ArrayList<Session> getDefaultBookmarks() {

        ArrayList<Session> defaultList = new ArrayList<Session>();

        defaultList.add(new Session("whoami.lua.powerdns.org", Type.TXT));
        defaultList.add(new Session("whoami-ecs.lua.powerdns.org", Type.TXT));
        defaultList.add(new Session("header.lua.powerdns.org", Type.TXT));
        defaultList.add(new Session("latlon.v4.powerdns.org", Type.TXT));
        defaultList.add(new Session("whoami.ds.akahelp.net", Type.TXT));

        return defaultList;

    }

}