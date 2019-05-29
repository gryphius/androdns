package androdns.android.leetdreams.ch.androdns;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;


public class StarredQueriesActivity extends ListActivity {

    private StarredQueries starredQueries;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        starredQueries = new StarredQueries(getApplicationContext());
        starredQueries.load();

        StarredQueryAdapter adapter = new StarredQueryAdapter(this,starredQueries.getStarredList().toArray(new Session[0]));
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("entry", position);
        resultIntent.putExtra("source", "starred");

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
