package androdns.android.leetdreams.ch.androdns;

import android.content.Context;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by gryphius on 30.04.17.
 */

public class History {
    private ArrayList<Session> historyvector = new ArrayList<Session>();
    private static final String historyFile = "history.json";
    private Context context =null;
    public History(Context context){
        this.context = context;
    }

    public void save(){
            SessionStorage.save(context,historyFile,historyvector);
    }

    public void load(){
        historyvector = SessionStorage.load(context,historyFile);
    }

    public void addEntry(Session s){
        historyvector.add(s);
        save();
    }

    public ArrayList<Session> getHistory(){
        ArrayList<Session> copy = new ArrayList<>(historyvector);
        Collections.reverse(copy);
        return copy;
    }

    public Session getSessionAt(int position){
        return getHistory().get(position);
    }
}