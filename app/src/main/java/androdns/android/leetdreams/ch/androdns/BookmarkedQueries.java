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
        SessionStorage.save(context, bookmarkFile,bookmarks);
    }


    public void load() {
        bookmarks = SessionStorage.load(context, bookmarkFile);

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