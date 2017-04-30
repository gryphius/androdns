package androdns.android.leetdreams.ch.androdns;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

/**
 * Created by gryphius on 30.04.17.
 */

public class History {



    private Vector<Session> historyvector = new Vector<Session>();
    private static final String historyFile = "history.dat";
    private Context context =null;
    public History(Context context){
        this.context = context;
    }

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

    public void load(){
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(context.openFileInput(historyFile));
            historyvector=(Vector<Session>) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEntry(Session s){
        historyvector.add(s);
        save();
    }

    public Vector<Session> getHistory(){
        return historyvector;
    }


}
