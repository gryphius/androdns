package androdns.android.leetdreams.ch.androdns;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;


public class HistoryActivity extends ListActivity {

    private History history;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        history = new History(getApplicationContext());
        history.load();

        HistoryAdapter adapter = new HistoryAdapter(this,history.getHistory().toArray(new Session[0]));
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("entry", position);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
