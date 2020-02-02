package androdns.android.leetdreams.ch.androdns;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class SessionStorage {

    public static void save(Context context, String filename, ArrayList<Session> sessions) {
        try {

            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, "UTF-8"));
            writer.setIndent("  ");
            writer.beginArray();
            for (Session s : sessions) {
                s.toJSON(writer);
            }
            writer.endArray();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // verify
        loadJSONStringFromFile(context, filename);
    }

    public static String loadJSONStringFromFile(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.openFileInput(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        Log.d("JSON",json);
        return json;
    }

    public static ArrayList<Session> load(Context context, String filename) {
        ArrayList<Session> sessions = new ArrayList<Session>();
        try {
            JSONTokener tokener = new JSONTokener(loadJSONStringFromFile(context,filename));
            JSONArray jsa = new JSONArray(tokener);
            for (int i = 0; i < jsa.length(); i++) {
                JSONObject obj = jsa.getJSONObject(i);
                Session s = new Session();
                try {
                    s.fromJSON(obj);
                    sessions.add(s);
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessions;
    }



}
