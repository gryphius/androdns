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


/**
 * This object represents the list of all bookmarked queries and is responsible for loading/saving
 * this list
 */
public class BookmarkedQueries {
    //The List of DNS Sessions we want to store/load.
    private ArrayList<Session> bookmarks = new ArrayList<Session>();

    // Filename where we store the list as JSON
    private static final String bookmarkFile = "bookmarks.json";

    // Application Context
    private Context context = null;

    public BookmarkedQueries(Context context) {
        this.context = context;
    }

    /**
     * save the current list of bookmarked sessions as json
     * This currently fails silently
     */
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

    /**
     * try to load the list of bookmarked sessions from json
     * @return the JSON as string if available, null otherwise
     */
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

    /**
     * load JSON from storage and try to parse it into the list of Session objects.
     * If this fails for any reason, return a default list of handy bookmarks
     */
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

    /**
     * add session to the list of bookmarks
     * does nothing if this session is already bookmarked
     * @param s
     */
    public void bookmark(Session s) {
        if(!isBookmarked(s)) {
            bookmarks.add(s);
        }
        save();
    }

    /**
     * remove the session from the list of bookmarks
     * @param s
     */
    public void removeBookmark(Session s) {
        bookmarks.remove(s);
        save();
    }

    /**
     *
     * @return current list of bookmarks
     */
    public ArrayList<Session> getBookmarks() {
        return bookmarks;
    }

    /**
     * replace the current bookmark list
     * @param newBookmarks
     */
    public void setBookmarks(ArrayList<Session> newBookmarks) {
        bookmarks = newBookmarks;
    }

    /**
     * returns true if the session is on the list of bookmarks
     * @param s
     * @return
     */
    public boolean isBookmarked(Session s){
        return bookmarks.contains(s);
    }

    /**
     * returns a specific bookmarked session
     *
     * @param position
     * @return
     */
    public Session getSessionAt(int position) {
        return getBookmarks().get(position);
    }

    /**
     * builds a list of default bookmarks, which are often useful in analyzing/debugging the DNS
     * such as the "use ful names" from PowerDNS: https://powerdns.org/useful-names/
     * @return
     */
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