package androdns.android.leetdreams.ch.androdns;

import android.content.Context;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;


/**
 * This class keeps track of the query history and handles persistence
 * Currently the query history is simply being serialized and potentially destroyed on application updates
 * We could change this to using JSON like we do for the bookmarks, but for now nobody has asked for it
 * Eventually we should think of how to make sure the list does not get too big
 */

public class History {
    // keeps the full history
    private ArrayList<Session> historyvector = new ArrayList<Session>();

    // filename where we serialize the history into
    private static final String historyFile = "history.dat";

    // application context
    private Context context =null;
    public History(Context context){
        this.context = context;
    }

    /**
     * serialize the current history into a file
     * fails silently
     */
    public void save(){
        try {
            ObjectOutputStream out;
            FileOutputStream fos = context.openFileOutput(historyFile, Context.MODE_PRIVATE);
            out = new ObjectOutputStream(fos);
            out.writeObject(historyvector);
            out.close();
        } catch(IOException e){
            e.printStackTrace();;
        }
    }

    /**
     *  try to deserialize the history file into a new history
     *  // fails silently
     */
    public void load(){
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(context.openFileInput(historyFile));
            historyvector=(ArrayList<Session>) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * add new entry to the history
     * @param s
     */
    public void addEntry(Session s){
        historyvector.add(s);
        save();
    }


    /**
     * get the full query history
     */
    public ArrayList<Session> getHistory(){
        Session[] history = historyvector.toArray(new Session[historyvector.size()]);

        ArrayList<Session> copy = new ArrayList<>(historyvector);

        Collections.reverse(copy);
        return copy;
    }

    /**
     * get specific history entry at position
     * @param position
     * @return
     */
    public Session getSessionAt(int position){
        return getHistory().get(position);
    }
}