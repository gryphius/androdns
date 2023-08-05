package androdns.android.leetdreams.ch.androdns;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ListView;


public class BookmarkedQueriesActivity extends ListActivity {

    private BookmarkedQueries bookmarkedQueries;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookmarkedQueries = new BookmarkedQueries(getApplicationContext());
        bookmarkedQueries.load();

        BookmarkedQueriesAdapter adapter = new BookmarkedQueriesAdapter(this, bookmarkedQueries.getBookmarks().toArray(new Session[0]));
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("entry", position);
        resultIntent.putExtra("source", "bookmarks");

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
